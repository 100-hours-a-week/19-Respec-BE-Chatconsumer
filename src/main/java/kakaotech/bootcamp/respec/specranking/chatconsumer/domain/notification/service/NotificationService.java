package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.notification.service;

import static kakaotech.bootcamp.respec.specranking.chatconsumer.global.common.type.NotificationTargetType.CHAT;

import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.notification.entity.Notification;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.notification.repository.NotificationRepository;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public void createChatNotificationIfNotExists(User receiver) {
        if (!notificationRepository.existsByUserIdAndTargetName(receiver.getId(), CHAT)) {
            notificationRepository.save(new Notification(receiver, CHAT));
        }
    }
}
