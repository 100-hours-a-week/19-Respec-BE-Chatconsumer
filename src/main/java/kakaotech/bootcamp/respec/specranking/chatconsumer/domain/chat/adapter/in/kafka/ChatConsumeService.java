package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.adapter.in.kafka;


import static kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.adapter.in.kafka.constant.ChatConsumeServiceConstant.IDEMPOTENCY_TTL;
import static kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.adapter.in.kafka.exception.InvalidChatEventStatusException.MESSAGE_INVALID_STATUS;
import static kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.exception.UserNotFoundException.MESSAGE_USER_NOT_FOUND;
import static kakaotech.bootcamp.respec.specranking.chatconsumer.global.common.type.ChatStatus.SENT;

import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.adapter.in.kafka.event.ChatConsumeEvent;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.adapter.in.kafka.exception.InvalidChatEventStatusException;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.adapter.in.kafka.mapping.ChatConsumeEventMapping;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.service.ChatCreationService;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.service.ChatDeliveryService;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.entity.User;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.exception.UserNotFoundException;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.repository.UserRepository;
import kakaotech.bootcamp.respec.specranking.chatconsumer.global.infrastructure.redis.service.IdempotencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ChatConsumeService {
    private final ChatDeliveryService chatDeliveryService;
    private final IdempotencyService idempotencyService;
    private final UserRepository userRepository;
    private final ChatCreationService chatCreationService;

    @KafkaListener(topics = "chat", containerFactory = "chatMessageContainerFactory")
    public void handleChatMessage(ChatConsumeEvent chatDto) {
        final String idempotentKey = chatDto.idempotentKey();

        try {
            if (!chatDto.status().equals(SENT)) {
                throw new InvalidChatEventStatusException(MESSAGE_INVALID_STATUS);
            }

            if (!idempotencyService.setIfAbsent(idempotentKey, IDEMPOTENCY_TTL)) {
                return;
            }

            User sender = findUser(chatDto.senderId());
            User receiver = findUser(chatDto.receiverId());

            chatCreationService.createChat(sender, receiver, chatDto.content());
            chatDeliveryService.relayOrNotify(receiver, ChatConsumeEventMapping.consumeToRelay(chatDto));
        } catch (Exception e) {
            if (idempotencyService.hasKey(idempotentKey)) {
                idempotencyService.delete(idempotentKey);
            }
            throw e;
        }
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(MESSAGE_USER_NOT_FOUND, id));
    }
}
