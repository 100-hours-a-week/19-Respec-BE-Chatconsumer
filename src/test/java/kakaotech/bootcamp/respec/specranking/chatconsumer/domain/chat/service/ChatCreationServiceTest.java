package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.service;

import static kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.fixture.ChatFixture.MESSAGE_FIXTURE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.entity.Chat;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.repository.ChatRepository;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chatparticipation.entity.ChatParticipation;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chatparticipation.repository.ChatParticipationRepository;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chatroom.entity.Chatroom;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chatroom.repository.ChatroomRepository;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.entity.User;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ChatCreationServiceTest {
    @InjectMocks
    ChatCreationService chatCreationService;

    @Mock
    ChatRepository chatRepository;

    @Mock
    ChatroomRepository chatroomRepository;

    @Mock
    ChatParticipationRepository chatParticipationRepository;

    @DisplayName("채팅방이 이미 존재하는 경우 기존 채팅방에 채팅을 저장한다")
    @Test
    void createChatInExistingChatroom() {
        // given
        User sender = createUserWithId(1L);
        User receiver = createUserWithId(2L);
        String content = MESSAGE_FIXTURE;

        Chatroom existingChatroom = createChatroomWithId(1L);

        given(chatroomRepository.findCommonChatroom(sender, receiver))
                .willReturn(Optional.of(existingChatroom));

        // when
        chatCreationService.createChat(sender, receiver, content);

        // then
        ArgumentCaptor<Chat> chatCaptor = ArgumentCaptor.forClass(Chat.class);
        verify(chatRepository).save(chatCaptor.capture());

        Chat savedChat = chatCaptor.getValue();
        assertThat(savedChat.getSender()).isEqualTo(sender);
        assertThat(savedChat.getReceiver()).isEqualTo(receiver);
        assertThat(savedChat.getChatroom()).isEqualTo(existingChatroom);
        assertThat(savedChat.getContent()).isEqualTo(content);

        verify(chatroomRepository, never()).save(any(Chatroom.class));
        verify(chatParticipationRepository, never()).save(any(ChatParticipation.class));
    }

    @DisplayName("채팅방이 존재하지 않는 경우 새로운 채팅방을 생성하고 채팅을 저장한다")
    @Test
    void createChatWithNewChatroom() {
        // given
        User sender = createUserWithId(1L);
        User receiver = createUserWithId(2L);
        String content = MESSAGE_FIXTURE;
        Chatroom newChatroom = createChatroomWithId(1L);

        given(chatroomRepository.findCommonChatroom(sender, receiver))
                .willReturn(Optional.empty());
        given(chatroomRepository.save(any(Chatroom.class)))
                .willReturn(newChatroom);

        // when
        chatCreationService.createChat(sender, receiver, content);

        // then
        verify(chatroomRepository).save(any(Chatroom.class));
        verify(chatParticipationRepository, times(2)).save(any(ChatParticipation.class));
        verify(chatRepository).save(any(Chat.class));
    }

    @DisplayName("새로운 채팅방 생성 시 sender와 receiver가 모두 참여자로 등록된다")
    @Test
    void createParticipationsWhenNewChatroomCreated() {
        // given
        User sender = createUserWithId(1L);
        User receiver = createUserWithId(2L);
        String content = MESSAGE_FIXTURE;
        Chatroom newChatroom = createChatroomWithId(1L);

        given(chatroomRepository.findCommonChatroom(sender, receiver))
                .willReturn(Optional.empty());
        given(chatroomRepository.save(any(Chatroom.class)))
                .willReturn(newChatroom);

        // when
        chatCreationService.createChat(sender, receiver, content);

        // then
        ArgumentCaptor<ChatParticipation> participationCaptor = ArgumentCaptor.forClass(ChatParticipation.class);
        verify(chatParticipationRepository, times(2)).save(participationCaptor.capture());

        List<ChatParticipation> capturedParticipations = participationCaptor.getAllValues();
        assertThat(capturedParticipations).hasSize(2);

        boolean hasSenderParticipation = capturedParticipations.stream()
                .anyMatch(p -> p.getUser().equals(sender) && p.getChatroom().equals(newChatroom));
        assertThat(hasSenderParticipation).isTrue();

        boolean hasReceiverParticipation = capturedParticipations.stream()
                .anyMatch(p -> p.getUser().equals(receiver) && p.getChatroom().equals(newChatroom));
        assertThat(hasReceiverParticipation).isTrue();
    }

    @DisplayName("새로운 채팅방 생성 시 채팅이 올바르게 저장된다")
    @Test
    void saveChatInNewChatroom() {
        // given
        User sender = createUserWithId(1L);
        User receiver = createUserWithId(2L);
        String content = MESSAGE_FIXTURE;
        Chatroom newChatroom = createChatroomWithId(1L);

        given(chatroomRepository.findCommonChatroom(sender, receiver))
                .willReturn(Optional.empty());
        given(chatroomRepository.save(any(Chatroom.class)))
                .willReturn(newChatroom);

        // when
        chatCreationService.createChat(sender, receiver, content);

        // then
        ArgumentCaptor<Chat> chatCaptor = ArgumentCaptor.forClass(Chat.class);
        verify(chatRepository).save(chatCaptor.capture());

        Chat savedChat = chatCaptor.getValue();
        assertThat(savedChat.getSender()).isEqualTo(sender);
        assertThat(savedChat.getReceiver()).isEqualTo(receiver);
        assertThat(savedChat.getChatroom()).isEqualTo(newChatroom);
        assertThat(savedChat.getContent()).isEqualTo(content);
    }

    private User createUserWithId(Long id) {
        User user = UserFixture.create();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Chatroom createChatroomWithId(Long id) {
        Chatroom chatroom = new Chatroom();
        ReflectionTestUtils.setField(chatroom, "id", id);
        return chatroom;
    }
}
