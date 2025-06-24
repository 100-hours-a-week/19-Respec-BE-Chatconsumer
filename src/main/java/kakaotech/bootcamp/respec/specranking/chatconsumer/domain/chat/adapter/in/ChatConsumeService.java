package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.adapter.in;


import static kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.adapter.in.exception.InvalidChatEventStatusException.MESSAGE_INVALID_STATUS;
import static kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.exception.UserNotFoundException.MESSAGE_USER_NOT_FOUND;
import static kakaotech.bootcamp.respec.specranking.chatconsumer.global.common.type.ChatStatus.SENT;

import java.time.Duration;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.adapter.in.Event.ChatConsumeEvent;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.adapter.in.exception.InvalidChatEventStatusException;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.adapter.in.mapping.ChatDtoMapping;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.entity.Chat;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.repository.ChatRepository;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.service.ChatDeliveryService;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chatparticipation.entity.ChatParticipation;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chatparticipation.repository.ChatParticipationRepository;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chatroom.entity.Chatroom;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chatroom.repository.ChatroomRepository;
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

    private static final Duration IDEMPOTENCY_TTL = Duration.ofMinutes(3);

    private final ChatDeliveryService chatDeliveryService;
    private final IdempotencyService idempotencyService;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final ChatroomRepository chatroomRepository;
    private final ChatParticipationRepository chatParticipationRepository;


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

            Chatroom chatroom = getOrCreateChatRoom(sender, receiver);

            chatRepository.save(new Chat(sender, receiver, chatroom, chatDto.content()));

            chatDeliveryService.relayOrNotify(receiver, ChatDtoMapping.consumeToRelay(chatDto));

        } catch (Exception e) {
            if (idempotencyService.hasKey(idempotentKey)) {
                idempotencyService.delete(idempotentKey);
            }
            throw e;
        }
    }

    private Chatroom getOrCreateChatRoom(User sender, User receiver) {
        return chatroomRepository.findCommonChatroom(sender, receiver)
                .orElseGet(() -> createNewChatRoom(sender, receiver));
    }

    private Chatroom createNewChatRoom(User sender, User receiver) {
        Chatroom chatroom = new Chatroom();
        Chatroom savedChatroom = chatroomRepository.save(chatroom);

        ChatParticipation senderParticipation = new ChatParticipation(savedChatroom, sender);
        chatParticipationRepository.save(senderParticipation);

        ChatParticipation receiverParticipation = new ChatParticipation(savedChatroom, receiver);
        chatParticipationRepository.save(receiverParticipation);

        return savedChatroom;
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(MESSAGE_USER_NOT_FOUND, id));
    }
}
