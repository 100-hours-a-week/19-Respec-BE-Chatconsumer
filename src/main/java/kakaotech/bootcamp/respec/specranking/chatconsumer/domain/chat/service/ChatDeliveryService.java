package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.service;

import java.util.Optional;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.dto.ChatRelayDto;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.notification.service.NotificationService;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.entity.User;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.service.UserServerLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatDeliveryService {
    private final UserServerLocationService userServerLocationService;
    private final ChatRelayService chatRelayService;
    private final NotificationService notificationService;

    public void relayOrNotify(User receiver, ChatRelayDto dto) {
        Optional<String> serverIp = userServerLocationService.getServerUrl(receiver.getId());
        if (serverIp.isPresent()) {
            chatRelayService.relay(serverIp.get(), dto);
        } else {
            notificationService.createChatNotificationIfNotExists(receiver);
        }
    }
}
