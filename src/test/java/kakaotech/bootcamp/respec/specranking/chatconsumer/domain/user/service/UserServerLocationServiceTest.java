package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.service;

import static kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.constant.UserServerLocationServiceConstant.NOT_CHAT_SESSION_TYPE_REDIS_VALUE_EXCEPTION;
import static kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.constant.UserServerLocationServiceConstant.REDIS_USER_KEY_PREFIX;
import static kakaotech.bootcamp.respec.specranking.chatconsumer.global.infrastructure.constant.UrlConstant.EXAMPLE_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.adapter.in.redis.dto.ChatSessionRedisValue;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.exception.InvalidRedisValueTypeException;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class UserServerLocationServiceTest {

    @InjectMocks
    UserServerLocationService userServerLocationService;

    @Mock
    RedisTemplate<String, Object> redisTemplate;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    ValueOperations<String, Object> valueOperations;

    final Long exampleUserId = UserFixture.getId();
    final String redisKey = REDIS_USER_KEY_PREFIX + exampleUserId;

    @DisplayName("Redis에 session 값이 있을 경우 Optional로 반환한다")
    @Test
    void returnsOptionalWhenSessionExistsInRedis() {
        // given
        final String exampleServerUrl = EXAMPLE_URL;
        final Object redisValue = new Object();
        final ChatSessionRedisValue chatSession = new ChatSessionRedisValue(exampleServerUrl, exampleUserId);

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(redisKey)).willReturn(redisValue);
        given(objectMapper.convertValue(redisValue, ChatSessionRedisValue.class)).willReturn(chatSession);

        // when
        Optional<String> result = userServerLocationService.getServerUrl(exampleUserId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(exampleServerUrl);
    }

    @DisplayName("Redis에 session 값이 없을 경우 빈 Optional을 반환한다")
    @Test
    void returnsEmptyOptionalWhenSessionDoesNotExistInRedis() {
        // given
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(redisKey)).willReturn(null);
        given(objectMapper.convertValue(null, ChatSessionRedisValue.class)).willReturn(null);

        // when
        Optional<String> result = userServerLocationService.getServerUrl(exampleUserId);

        // then
        assertThat(result).isEmpty();
    }

    @DisplayName("Redis에 session 타입이 ChatSessionRedisValue가 아닌 경우 예외가 발생한다")
    @Test
    void throwsExceptionWhenRedisValueIsNotChatSessionRedisValue() {
        // given
        final Integer wrongTypeValueExample = 12345;

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(redisKey)).willReturn(wrongTypeValueExample);
        given(objectMapper.convertValue(wrongTypeValueExample, ChatSessionRedisValue.class))
                .willThrow(new IllegalArgumentException(NOT_CHAT_SESSION_TYPE_REDIS_VALUE_EXCEPTION));

        // when & then
        assertThatThrownBy(() -> userServerLocationService.getServerUrl(exampleUserId))
                .isInstanceOf(InvalidRedisValueTypeException.class)
                .hasMessageContaining(NOT_CHAT_SESSION_TYPE_REDIS_VALUE_EXCEPTION);
    }
}
