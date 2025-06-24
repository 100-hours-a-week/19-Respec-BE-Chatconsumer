package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.notification.service;

import static kakaotech.bootcamp.respec.specranking.chatconsumer.global.common.type.NotificationTargetType.CHAT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.notification.entity.Notification;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.notification.repository.NotificationRepository;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    NotificationService notificationService;

    @Mock
    NotificationRepository notificationRepository;

    private final Long RECEIVER_ID = 1L;
    private final String LOGIN_ID = "test@example.com";
    private final String PASSWORD = "password";
    private final String PROFILE_IMAGE_URL = "http://example.com/profile.jpg";
    private final String NICKNAME = "nickname";
    private final boolean IS_OPEN_SPEC = true;
    private final String PRIMARY_KEY_FIELD = "id";

    private User receiver;

    @BeforeEach
    void setUp() {
        receiver = new User(LOGIN_ID, PASSWORD, PROFILE_IMAGE_URL, NICKNAME, IS_OPEN_SPEC);
        ReflectionTestUtils.setField(receiver, PRIMARY_KEY_FIELD, RECEIVER_ID);
    }

    @Test
    @DisplayName("기존 알림이 없으면 알림을 생성한다")
    void creates_notification_when_not_exists() {
        // given
        given(notificationRepository.existsByUserIdAndTargetName(RECEIVER_ID, CHAT)).willReturn(false);

        // when
        notificationService.createChatNotificationIfNotExists(receiver);

        // then
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("기존 알림이 있으면 저장하지 않는다")
    void does_not_create_notification_when_exists() {
        // given
        given(notificationRepository.existsByUserIdAndTargetName(RECEIVER_ID, CHAT)).willReturn(true);

        // when
        notificationService.createChatNotificationIfNotExists(receiver);

        // then
        verify(notificationRepository, never()).save(any());
    }
}
