package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.fixture;

import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.dto.ChatRelayDto;

public class ChatRelayFixture {

    private static final Long senderId = 1L;
    private static final Long receiverId = 2L;
    private static final String CHAT_MESSAGE = "안녕하세요!";

    public static ChatRelayDto create() {
        return new ChatRelayDto(senderId, receiverId, CHAT_MESSAGE);
    }
}
