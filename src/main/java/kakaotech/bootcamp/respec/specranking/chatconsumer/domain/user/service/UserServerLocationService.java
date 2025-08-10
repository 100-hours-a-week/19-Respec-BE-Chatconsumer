package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.service;

import static kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.constant.UserServerLocationServiceConstant.NOT_STRING_REDIS_VALUE_EXCEPTION;
import static kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.constant.UserServerLocationServiceConstant.REDIS_USER_KEY_PREFIX;

import java.util.Optional;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.adapter.in.redis.dto.ChatSessionRedisValue;
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

    public Optional<String> getServerUrl(Long partnerId) {
        Object serverUrlObj = redisTemplate.opsForValue().get(REDIS_USER_KEY_PREFIX + partnerId);

        ChatSessionRedisValue chatSessionRedisValue = null;
        try {
            chatSessionRedisValue = objectMapper.convertValue(serverUrlObj,
                    ChatSessionRedisValue.class);
        } catch (IllegalArgumentException e) {
            throw new InvalidRedisValueTypeException(
                    NOT_STRING_REDIS_VALUE_EXCEPTION + " key=" + REDIS_USER_KEY_PREFIX + partnerId);
        }

        if (chatSessionRedisValue != null && chatSessionRedisValue.partnerId().equals(partnerId)) {
            return Optional.of(chatSessionRedisValue.privateAddress());
        }

        return Optional.empty();
    }
}
