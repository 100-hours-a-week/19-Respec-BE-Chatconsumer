package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.notification.repository;

import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.notification.entity.Notification;
import kakaotech.bootcamp.respec.specranking.chatconsumer.global.common.type.NotificationTargetType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    boolean existsByUserIdAndTargetName(Long userId, NotificationTargetType targetName);
}
