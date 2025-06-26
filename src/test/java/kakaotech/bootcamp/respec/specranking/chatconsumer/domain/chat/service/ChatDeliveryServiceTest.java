package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.service;

import static kakaotech.bootcamp.respec.specranking.chatconsumer.global.infrastructure.constant.UrlConstant.EXAMPLE_URL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.dto.ChatRelayDto;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.fixture.ChatRelayFixture;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.notification.service.NotificationService;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.entity.User;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.fixture.UserFixture;
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

    private User receiver;
    private ChatRelayDto chatRelayDto;


    @BeforeEach
    void setUp() {
        receiver = UserFixture.create();
        chatRelayDto = ChatRelayFixture.create();
    }

    @Test
    @DisplayName("Optional에 서버 IP가 있을 경우 relay가 호출된다")
    void calls_relay_when_server_ip_exists() {
        // given
        final String serverIpExample = EXAMPLE_URL;

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
