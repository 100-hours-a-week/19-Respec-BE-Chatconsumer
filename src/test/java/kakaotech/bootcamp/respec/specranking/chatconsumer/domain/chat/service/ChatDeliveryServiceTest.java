package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.dto.ChatRelayDto;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.notification.service.NotificationService;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.entity.User;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.service.UserServerLocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatDeliveryServiceTest {

    @InjectMocks
    ChatDeliveryService chatDeliveryService;

    @Mock
    UserServerLocationService userServerLocationService;

    @Mock
    ChatRelayService chatRelayService;

    @Mock
    NotificationService notificationService;

    private final Long senderId = 1L;
    private final Long receiverId = 2L;
    private final String CHAT_MESSAGE = "안녕하세요!";
    private final String LOGIN_ID = "receiver@example.com";
    private final String PASSWORD = "receiver-password";
    private final String PROFILE_IMAGE_URL = "http://example.profile.jpg";
    private final String NICKNAME = "http://example.profile.jpg";
    private final Boolean isOpenSpec = true;

    private User receiver;
    private ChatRelayDto chatRelayDto;


    @BeforeEach
    void setUp() {
        receiver = new User(LOGIN_ID, PASSWORD, PROFILE_IMAGE_URL, NICKNAME, isOpenSpec);
        chatRelayDto = new ChatRelayDto(senderId, receiverId, CHAT_MESSAGE);
    }

    @Test
    @DisplayName("Optional에 서버 IP가 있을 경우 relay가 호출된다")
    void calls_relay_when_server_ip_exists() {
        // given
        final String serverIpExample = "192.168.1.100:8080";

        given(userServerLocationService.getServerUrl(receiver.getId())).willReturn(Optional.of(serverIpExample));

        // when
        chatDeliveryService.relayOrNotify(receiver, chatRelayDto);

        // then
        verify(chatRelayService).relay(serverIpExample, chatRelayDto);
        verify(notificationService, never()).createChatNotificationIfNotExists(any());
    }

    @Test
    @DisplayName("서버 IP가 빈 Optional일 경우 notification이 호출된다")
    void calls_notification_when_server_ip_absent() {
        // given
        given(userServerLocationService.getServerUrl(receiver.getId())).willReturn(Optional.empty());

        // when
        chatDeliveryService.relayOrNotify(receiver, chatRelayDto);

        // then
        verify(notificationService).createChatNotificationIfNotExists(receiver);
        verify(chatRelayService, never()).relay(anyString(), any());
    }

}
