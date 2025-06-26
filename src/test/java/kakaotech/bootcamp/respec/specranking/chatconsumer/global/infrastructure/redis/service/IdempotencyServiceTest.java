package kakaotech.bootcamp.respec.specranking.chatconsumer.global.infrastructure.redis.service;

import static kakaotech.bootcamp.respec.specranking.chatconsumer.global.infrastructure.redis.constant.IdempotencyServiceConstant.DUMMY_VALUE;
import static kakaotech.bootcamp.respec.specranking.chatconsumer.global.infrastructure.redis.constant.IdempotencyServiceTestConstant.KEY_VALUE;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

    @InjectMocks
    IdempotencyService idempotencyService;

    @Mock
    RedisTemplate<String, Object> redisTemplate;

    @Mock
    ValueOperations<String, Object> valueOperations;

    @Test
    @DisplayName("setIfAbsent 호출 시 키가 없을 경우 키를 등록한다")
    void setIfAbsentWhenKeyIsAbsentThenRegistersKey() {
        // given
        final String newKey = KEY_VALUE;
        final long DUMMY_SECONDS = 5;
        final Duration ttl = Duration.ofSeconds(DUMMY_SECONDS);

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.setIfAbsent(newKey, DUMMY_VALUE, ttl)).willReturn(true);

        // when
        idempotencyService.setIfAbsent(newKey, ttl);

        // then
        verify(valueOperations).setIfAbsent(newKey, DUMMY_VALUE, ttl);
    }

    @Test
    @DisplayName("setIfAbsent 호출 시 키가 있을 경우 false를 반환한다")
    void setIfAbsentWhenKeyExistsThenReturnsFalse() {
        // given
        final String existingKey = KEY_VALUE;
        final long DUMMY_SECONDS = 5;
        final Duration ttl = Duration.ofSeconds(DUMMY_SECONDS);

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.setIfAbsent(existingKey, DUMMY_VALUE, ttl)).willReturn(false);

        // when
        final Boolean result = idempotencyService.setIfAbsent(existingKey, ttl);

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("hasKey 호출 시 키가 있을 경우 true를 반환한다")
    void hasKeyWhenKeyExistsThenReturnsTrue() {
        // given
        final String existingKey = KEY_VALUE;
        given(redisTemplate.hasKey(existingKey)).willReturn(true);

        // when
        final boolean result = idempotencyService.hasKey(existingKey);

        // then
        assertTrue(result);
    }


    @Test
    @DisplayName("hasKey 호출 시 키가 없을 경우 false를 반환한다")
    void hasKeyWhenKeyIsAbsentThenReturnsFalse() {
        // given
        final String absentKey = KEY_VALUE;
        given(redisTemplate.hasKey(absentKey)).willReturn(false);

        // when
        final boolean result = idempotencyService.hasKey(absentKey);

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("delete 호출 시 해당 키가 삭제되어야 한다")
    void deleteWhenCalledThenDeletesKey() {
        // given
        final String keyToBeDeleted = KEY_VALUE;

        // when
        idempotencyService.delete(keyToBeDeleted);

        // then
        verify(redisTemplate).delete(keyToBeDeleted);
    }
}
