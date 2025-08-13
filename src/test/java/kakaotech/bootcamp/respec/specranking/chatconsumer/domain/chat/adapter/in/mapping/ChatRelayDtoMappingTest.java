package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.adapter.in.mapping;

import static org.assertj.core.api.Assertions.assertThat;

import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.adapter.in.fixture.ChatConsumeEventFixture;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.adapter.in.kafka.event.ChatConsumeEvent;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.adapter.in.kafka.mapping.ChatConsumeEventMapping;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.dto.ChatRelayDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ChatRelayDtoMappingTest {

    @Test
    @DisplayName("ChatConsumeEvent → ChatRelayDto로 정상 변환된다")
    void consumeToRelay_successfullyConverts() {
        // given
        ChatConsumeEvent consumeEvent = ChatConsumeEventFixture.createSuccessfulChatEvent();

        // when
        ChatRelayDto relayDto = ChatConsumeEventMapping.consumeToRelay(consumeEvent);

        // then
        assertThat(relayDto.senderId()).isEqualTo(consumeEvent.senderId());
        assertThat(relayDto.receiverId()).isEqualTo(consumeEvent.receiverId());
        assertThat(relayDto.content()).isEqualTo(consumeEvent.content());
    }
}
