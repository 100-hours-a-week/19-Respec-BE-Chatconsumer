package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.service;

import jakarta.transaction.Transactional;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.entity.Chat;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.repository.ChatRepository;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chatparticipation.entity.ChatParticipation;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chatparticipation.repository.ChatParticipationRepository;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chatroom.entity.Chatroom;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chatroom.repository.ChatroomRepository;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatCreationService {
    private final ChatRepository chatRepository;
    private final ChatroomRepository chatroomRepository;
    private final ChatParticipationRepository chatParticipationRepository;

    public void createChat(User sender, User receiver, String content) {
        Chatroom chatroom = getOrCreateChatRoom(sender, receiver);
        chatRepository.save(new Chat(sender, receiver, chatroom, content));
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
}
