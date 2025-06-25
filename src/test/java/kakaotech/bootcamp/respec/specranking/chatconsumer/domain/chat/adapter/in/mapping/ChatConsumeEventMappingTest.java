package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.adapter.in.mapping;

import static org.assertj.core.api.Assertions.assertThat;

import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.adapter.in.Event.ChatConsumeEvent;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.adapter.in.fixture.ChatConsumeEventFixture;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.dto.ChatRelayDto;
import org.junit.jupiter.api.Test;

class ChatConsumeEventMappingTest {

    @Test
    void consumeToRelay_정상_변환() {
        // given
        ChatConsumeEvent consumeEvent = ChatConsumeEventFixture.create();

        // when
        ChatRelayDto relayDto = ChatConsumeEventMapping.consumeToRelay(consumeEvent);

        // then
        assertThat(relayDto.senderId()).isEqualTo(consumeEvent.senderId());
        assertThat(relayDto.receiverId()).isEqualTo(consumeEvent.receiverId());
        assertThat(relayDto.content()).isEqualTo(consumeEvent.content());
    }
}
