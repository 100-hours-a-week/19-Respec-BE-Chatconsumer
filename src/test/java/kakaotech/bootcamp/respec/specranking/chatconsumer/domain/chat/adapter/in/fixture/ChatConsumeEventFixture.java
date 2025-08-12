package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.adapter.in.fixture;

import static kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.fixture.ChatFixture.MESSAGE_FIXTURE;
import static kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.fixture.ChatFixture.RECEIVER_ID_FIXTURE;
import static kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.fixture.ChatFixture.SENDER_ID_FIXTURE;
import static kakaotech.bootcamp.respec.specranking.chatconsumer.global.common.type.ChatStatus.ERROR;
import static kakaotech.bootcamp.respec.specranking.chatconsumer.global.common.type.ChatStatus.SENT;
import static kakaotech.bootcamp.respec.specranking.chatconsumer.global.infrastructure.redis.constant.IdempotencyServiceTestConstant.KEY_VALUE;

import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.adapter.in.kafka.event.ChatConsumeEvent;

public class ChatConsumeEventFixture {
    public static ChatConsumeEvent createSuccessfulChatEvent() {
        return new ChatConsumeEvent(KEY_VALUE, SENDER_ID_FIXTURE, RECEIVER_ID_FIXTURE, MESSAGE_FIXTURE, SENT);
    }

    public static ChatConsumeEvent createErrorChatEvent() {
        return new ChatConsumeEvent(KEY_VALUE, SENDER_ID_FIXTURE, RECEIVER_ID_FIXTURE, MESSAGE_FIXTURE, ERROR);
    }
}
