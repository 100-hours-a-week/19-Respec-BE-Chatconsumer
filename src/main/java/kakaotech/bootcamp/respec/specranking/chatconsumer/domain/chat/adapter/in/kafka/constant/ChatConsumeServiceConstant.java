package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.adapter.in.kafka.constant;

import java.time.Duration;

public class ChatConsumeServiceConstant {
    public static final Duration IDEMPOTENCY_TTL = Duration.ofMinutes(3);
}
