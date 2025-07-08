package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.service;

import static kakaotech.bootcamp.respec.specranking.chatconsumer.global.common.type.NotificationTargetType.CHAT;

import com.fasterxml.jackson.databind.ObjectMapper;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.adapter.in.redis.dto.ChatSessionRedisValue;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.dto.ChatRelayDto;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.notification.entity.Notification;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.notification.repository.NotificationRepository;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatRelayService {

    private static final String REDIS_USER_KEY_PREFIX = "chat:user:";
    private static final String CHAT_RELAY_API_PATH = "/api/chat/relay";
    private static final String SCHEME = "http://";

    private final RedisTemplate<String, Object> redisTemplate;
    private final WebClient webClient;
    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    public void relayOrNotify(User receiver, ChatRelayDto dto) {
        Object serverIpObj = redisTemplate.opsForValue().get(REDIS_USER_KEY_PREFIX + receiver.getId());
        ChatSessionRedisValue chatSessionRedisValue = objectMapper.convertValue(serverIpObj,
                ChatSessionRedisValue.class);

        if (chatSessionRedisValue != null && chatSessionRedisValue.partnerId().equals(receiver.getId())) {
            final String serverIp = chatSessionRedisValue.privateAddress();
            webClient.post()
                    .uri(SCHEME + serverIp + CHAT_RELAY_API_PATH)
                    .bodyValue(dto)
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe();
        } else {
            if (notificationRepository.existsByUserIdAndTargetName(receiver.getId(), CHAT)) {
                return;
            }
            notificationRepository.save(new Notification(receiver, CHAT));
        }
    }
}
