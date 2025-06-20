package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.dto.consume;

import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.common.type.ChatStatus;

public record ChatConsumeDto(
        String idempotentKey,
        Long senderId,
        Long receiverId,
        String content,
        ChatStatus status
) {
}
