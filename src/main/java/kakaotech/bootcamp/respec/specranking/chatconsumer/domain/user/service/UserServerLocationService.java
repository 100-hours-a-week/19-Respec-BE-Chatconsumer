package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.service;

import static kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.constant.UserServerLocationServiceConstant.NOT_STRING_REDIS_VALUE_EXCEPTION;
import static kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.constant.UserServerLocationServiceConstant.REDIS_USER_KEY_PREFIX;

import java.util.Optional;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.exception.InvalidRedisValueTypeException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class UserServerLocationService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public Optional<String> getServerUrl(Long userId) {
        Object serverUrlObj = redisTemplate.opsForValue().get(REDIS_USER_KEY_PREFIX + userId);

        if (serverUrlObj == null) {
            return Optional.empty();
        }

        ChatSessionRedisValue chatSessionRedisValue = objectMapper.convertValue(serverIpObj,
                ChatSessionRedisValue.class);

        if (chatSessionRedisValue != null && chatSessionRedisValue.partnerId().equals(receiver.getId()) {
            final String serverIp = chatSessionRedisValue.privateAddress();
            return Optional.of(serverIp);
        }

        return Optional.empty();
    }
}
