package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.fixture;

import static kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.fixture.ChatFixture.MESSAGE_FIXTURE;

import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.dto.ChatRelayDto;

public class ChatRelayFixture {

    private static final Long senderId = 1L;
    private static final Long receiverId = 2L;

    public static ChatRelayDto create() {
        return new ChatRelayDto(senderId, receiverId, MESSAGE_FIXTURE);
    }
}
