package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.adapter.in.kafka.Event;

import kakaotech.bootcamp.respec.specranking.chatconsumer.global.common.type.ChatStatus;

public record ChatConsumeEvent(
        String idempotentKey,
        Long senderId,
        Long receiverId,
        String content,
        ChatStatus status
) {
}
