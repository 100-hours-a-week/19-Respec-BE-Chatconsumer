package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.notification.service;

import static kakaotech.bootcamp.respec.specranking.chatconsumer.global.common.type.NotificationTargetType.CHAT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.notification.entity.Notification;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.notification.repository.NotificationRepository;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.entity.User;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.fixture.UserFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    NotificationService notificationService;

    @Mock
    NotificationRepository notificationRepository;

    private User receiver;

    @BeforeEach
    void setUp() {
        receiver = UserFixture.create();
    }

    @Test
    @DisplayName("기존 알림이 없으면 알림을 생성한다")
    void createsNotificationWhenNotExists() {
        // given
        given(notificationRepository.existsByUserIdAndTargetName(receiver.getId(), CHAT)).willReturn(false);

        // when
        notificationService.createChatNotificationIfNotExists(receiver);

        // then
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("기존 알림이 있으면 저장하지 않는다")
    void doesNotCreateNotificationWhenExists() {
        // given
        given(notificationRepository.existsByUserIdAndTargetName(receiver.getId(), CHAT)).willReturn(true);

        // when
        notificationService.createChatNotificationIfNotExists(receiver);

        // then
        verify(notificationRepository, never()).save(any());
    }
}
