package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.service;


import static kakaotech.bootcamp.respec.specranking.chatconsumer.domain.common.type.ChatStatus.SENT;

import java.time.Duration;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.dto.consume.ChatConsumeDto;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.dto.mapping.ChatDtoMapping;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.entity.Chat;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.repository.ChatRepository;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chatparticipation.entity.ChatParticipation;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chatparticipation.repository.ChatParticipationRepository;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chatroom.entity.Chatroom;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chatroom.repository.ChatroomRepository;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.entity.User;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.repository.UserRepository;
import kakaotech.bootcamp.respec.specranking.chatconsumer.global.service.IdempotencyService;
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

    private final RelayService relayService;
    private final IdempotencyService idempotencyService;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final ChatroomRepository chatroomRepository;
    private final ChatParticipationRepository chatParticipationRepository;


    @KafkaListener(topics = "chat", containerFactory = "chatMessageContainerFactory")
    public void handleChatMessage(ChatConsumeDto chatDto) {
        try {

            if (!chatDto.status().equals(SENT)) {
                throw new IllegalArgumentException("Chat message is not sent");
            }

            if (!idempotencyService.setIfAbsent(chatDto.idempotentKey(), IDEMPOTENCY_TTL)) {
                return;
            }

            User sender = findUser(chatDto.senderId());
            User receiver = findUser(chatDto.receiverId());

            Chatroom chatroom = getOrCreateChatRoom(sender, receiver);

            chatRepository.save(new Chat(sender, receiver, chatroom, chatDto.content()));

            relayService.relayOrNotify(receiver, ChatDtoMapping.consumeToRelay(chatDto));
            
        } catch (Exception e) {
            log.error("Error processing chat message", e);
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
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }
}
