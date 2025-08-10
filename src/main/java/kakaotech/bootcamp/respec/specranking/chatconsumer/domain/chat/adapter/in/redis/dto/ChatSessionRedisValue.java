package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.adapter.in.redis.dto;

public record ChatSessionRedisValue(
        String privateAddress,
        Long partnerId
) {
}
