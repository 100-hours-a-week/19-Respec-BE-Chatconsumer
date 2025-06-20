package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.dto.relay;

public record ChatRelayDto(
        Long senderId,
        Long receiverId,
        String content
) {
}
