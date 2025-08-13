package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.adapter.in.kafka;

import static kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.adapter.in.kafka.constant.ChatConsumeServiceConstant.IDEMPOTENCY_TTL;
import static kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.fixture.ChatFixture.MESSAGE_FIXTURE;
import static kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.fixture.ChatFixture.RECEIVER_ID_FIXTURE;
import static kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.fixture.ChatFixture.SENDER_ID_FIXTURE;
import static kakaotech.bootcamp.respec.specranking.chatconsumer.global.common.type.ChatStatus.ERROR;
import static kakaotech.bootcamp.respec.specranking.chatconsumer.global.common.type.ChatStatus.SENT;
import static kakaotech.bootcamp.respec.specranking.chatconsumer.global.infrastructure.redis.constant.IdempotencyServiceTestConstant.KEY_VALUE;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import java.util.Optional;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.adapter.in.fixture.ChatConsumeEventFixture;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.adapter.in.kafka.event.ChatConsumeEvent;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.adapter.in.kafka.exception.InvalidChatEventStatusException;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.service.ChatCreationService;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.service.ChatDeliveryService;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.entity.User;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.exception.UserNotFoundException;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.fixture.UserFixture;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.repository.UserRepository;
import kakaotech.bootcamp.respec.specranking.chatconsumer.global.common.type.ChatStatus;
import kakaotech.bootcamp.respec.specranking.chatconsumer.global.infrastructure.redis.service.IdempotencyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ChatConsumeServiceTest {
    @InjectMocks
    private ChatConsumeService chatConsumeService;

    @Mock
    private ChatDeliveryService chatDeliveryService;

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChatCreationService chatCreationService;

    @Test
    @DisplayName("정상적인 채팅 메시지를 성공적으로 처리한다")
    void success_normalChatMessage() {
        // given
        ChatConsumeEvent event = ChatConsumeEventFixture.createSuccessfulChatEvent();
        User sender = createUserWithId(SENDER_ID_FIXTURE);
        User receiver = createUserWithId(RECEIVER_ID_FIXTURE);

        given(idempotencyService.setIfAbsent(KEY_VALUE, IDEMPOTENCY_TTL)).willReturn(true);
        given(userRepository.findById(SENDER_ID_FIXTURE)).willReturn(Optional.of(sender));
        given(userRepository.findById(RECEIVER_ID_FIXTURE)).willReturn(Optional.of(receiver));

        // when
        chatConsumeService.handleChatMessage(event);

        // then
        then(chatCreationService).should(times(1)).createChat(sender, receiver, MESSAGE_FIXTURE);
        then(chatDeliveryService).should(times(1)).relayOrNotify(eq(receiver), any());
    }

    @Test
    @DisplayName("상태가 SENT가 아닌 경우 InvalidChatEventStatusException을 발생시킨다")
    void throwException_whenStatusIsNotSent() {
        // given
        ChatConsumeEvent event = createChatConsumeEvent(KEY_VALUE, SENDER_ID_FIXTURE, RECEIVER_ID_FIXTURE,
                MESSAGE_FIXTURE, ERROR);

        // when & then
        assertThatThrownBy(() -> chatConsumeService.handleChatMessage(event))
                .isInstanceOf(InvalidChatEventStatusException.class);
    }

    @Test
    @DisplayName("중복 메시지인 경우 처리를 건너뛴다")
    void skip_whenDuplicateMessage() {
        // given
        ChatConsumeEvent event = createChatConsumeEvent(KEY_VALUE, SENDER_ID_FIXTURE, RECEIVER_ID_FIXTURE,
                MESSAGE_FIXTURE, SENT);

        given(idempotencyService.setIfAbsent(KEY_VALUE, IDEMPOTENCY_TTL)).willReturn(false);

        // when
        chatConsumeService.handleChatMessage(event);

        // then
        then(chatCreationService).should(never()).createChat(any(), any(), any());
        then(chatDeliveryService).should(never()).relayOrNotify(any(), any());
    }

    @Test
    @DisplayName("발신자를 찾을 수 없는 경우 UserNotFoundException을 발생시킨다")
    void throwException_whenSenderNotFound() {
        // given
        ChatConsumeEvent event = createChatConsumeEvent(KEY_VALUE, SENDER_ID_FIXTURE, RECEIVER_ID_FIXTURE,
                MESSAGE_FIXTURE, SENT);

        given(idempotencyService.setIfAbsent(KEY_VALUE, IDEMPOTENCY_TTL)).willReturn(true);
        given(userRepository.findById(SENDER_ID_FIXTURE)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> chatConsumeService.handleChatMessage(event))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("수신자를 찾을 수 없는 경우 UserNotFoundException을 발생시킨다")
    void throwException_whenReceiverNotFound() {
        // given
        ChatConsumeEvent event = createChatConsumeEvent(KEY_VALUE, SENDER_ID_FIXTURE, RECEIVER_ID_FIXTURE,
                MESSAGE_FIXTURE, SENT);
        User sender = createUserWithId(SENDER_ID_FIXTURE);

        given(idempotencyService.setIfAbsent(KEY_VALUE, IDEMPOTENCY_TTL)).willReturn(true);
        given(userRepository.findById(SENDER_ID_FIXTURE)).willReturn(Optional.of(sender));
        given(userRepository.findById(RECEIVER_ID_FIXTURE)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> chatConsumeService.handleChatMessage(event))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("동일한 메시지가 두 번 처리될 때 첫 번째만 처리된다")
    void processOnlyFirstMessage_whenDuplicateMessagesReceived() {
        // given
        ChatConsumeEvent event = createChatConsumeEvent(KEY_VALUE, SENDER_ID_FIXTURE, RECEIVER_ID_FIXTURE,
                MESSAGE_FIXTURE, SENT);
        User sender = createUserWithId(SENDER_ID_FIXTURE);
        User receiver = createUserWithId(RECEIVER_ID_FIXTURE);

        given(idempotencyService.setIfAbsent(KEY_VALUE, IDEMPOTENCY_TTL))
                .willReturn(true).willReturn(false);
        given(userRepository.findById(SENDER_ID_FIXTURE)).willReturn(Optional.of(sender));
        given(userRepository.findById(RECEIVER_ID_FIXTURE)).willReturn(Optional.of(receiver));

        // when
        chatConsumeService.handleChatMessage(event);
        chatConsumeService.handleChatMessage(event);

        // then
        then(chatCreationService).should(times(1)).createChat(sender, receiver, MESSAGE_FIXTURE);
        then(chatDeliveryService).should(times(1)).relayOrNotify(eq(receiver), any());
        then(idempotencyService).should(times(2)).setIfAbsent(KEY_VALUE, IDEMPOTENCY_TTL);
    }

    private ChatConsumeEvent createChatConsumeEvent(String idempotentKey, Long senderId,
                                                    Long receiverId, String content, ChatStatus status) {
        return new ChatConsumeEvent(idempotentKey, senderId, receiverId, content, status);
    }

    private User createUserWithId(Long id) {
        User user = UserFixture.create();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
