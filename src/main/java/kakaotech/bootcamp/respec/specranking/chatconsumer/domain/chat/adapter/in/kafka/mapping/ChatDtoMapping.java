package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.adapter.in.kafka.mapping;

import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.adapter.in.kafka.Event.ChatConsumeEvent;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.dto.ChatRelayDto;

public class ChatDtoMapping {

    public static ChatRelayDto consumeToRelay(ChatConsumeEvent consumeDto) {
        return new ChatRelayDto(
                consumeDto.senderId(),
                consumeDto.receiverId(),
                consumeDto.content()
        );
    }
}
