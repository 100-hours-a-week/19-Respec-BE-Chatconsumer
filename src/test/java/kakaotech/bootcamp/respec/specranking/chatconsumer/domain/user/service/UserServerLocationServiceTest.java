package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.service;

import static kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.constant.UserServerLocationServiceConstant.NOT_STRING_REDIS_VALUE_EXCEPTION;
import static kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.constant.UserServerLocationServiceConstant.REDIS_USER_KEY_PREFIX;
import static kakaotech.bootcamp.respec.specranking.chatconsumer.global.infrastructure.constant.UrlConstant.EXAMPLE_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;
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
    ValueOperations<String, Object> valueOperations;

    final Long exampleUserId = UserFixture.getId();
    final String redisKey = REDIS_USER_KEY_PREFIX + exampleUserId;

    @DisplayName("Redis에 서버 URL이 있을 경우 Optional로 반환한다")
    @Test
    void returns_optional_when_server_url_exists_in_redis() {
        // given
        final String exampleServerUrl = EXAMPLE_URL;

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(redisKey)).willReturn(exampleServerUrl);

        // when
        Optional<String> result = userServerLocationService.getServerUrl(exampleUserId);

        // then
        verify(valueOperations).get(redisKey);
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(exampleServerUrl);
    }

    @DisplayName("Redis에 서버 URL이 없을 경우 빈 Optional을 반환한다")
    @Test
    void returns_empty_optional_when_server_url_does_not_exist_in_redis() {
        // given
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(redisKey)).willReturn(null);

        // when
        Optional<String> result = userServerLocationService.getServerUrl(exampleUserId);

        // then
        verify(valueOperations).get(redisKey);
        assertThat(result).isEmpty();
    }

    @DisplayName("Redis에 서버 URL 타입이 String이 아닌 경우 예외가 발생한다")
    @Test
    void throws_exception_when_redis_value_is_not_string() {
        // given
        final Integer wrongTypeValueExample = 12345;

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(redisKey)).willReturn(wrongTypeValueExample);

        // when & then
        assertThatThrownBy(() -> userServerLocationService.getServerUrl(exampleUserId))
                .isInstanceOf(InvalidRedisValueTypeException.class)
                .hasMessageContaining(NOT_STRING_REDIS_VALUE_EXCEPTION);
    }
}
