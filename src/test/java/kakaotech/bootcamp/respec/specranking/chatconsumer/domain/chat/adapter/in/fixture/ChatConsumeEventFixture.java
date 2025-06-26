package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.adapter.in.fixture;

import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.adapter.in.Event.ChatConsumeEvent;
import kakaotech.bootcamp.respec.specranking.chatconsumer.global.common.type.ChatStatus;

public class ChatConsumeEventFixture {

    private static final String IDEMPOTENT_KEY = "idemp-1234";
    private static final Long SENDER_ID = 1L;
    private static final Long RECEIVER_ID = 2L;
    private static final String CHAT_MESSAGE = "안녕하세요!";
    private static final ChatStatus CHAT_STATUS = ChatStatus.SENT;

    public static ChatConsumeEvent create() {
        return new ChatConsumeEvent(IDEMPOTENT_KEY, SENDER_ID, RECEIVER_ID, CHAT_MESSAGE, CHAT_STATUS);
    }
}
