package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServerLocationService {
    private static final String REDIS_USER_KEY_PREFIX = "chat:user:";
    private final RedisTemplate<String, Object> redisTemplate;

    public Optional<String> getServerUrl(Long userId) {
        Object serverUrlObj = redisTemplate.opsForValue().get(REDIS_USER_KEY_PREFIX + userId);

        if (serverUrlObj instanceof String serverUrl && !serverUrl.isEmpty()) {
            return Optional.of(serverUrl);
        }
        return Optional.empty();
    }
}
