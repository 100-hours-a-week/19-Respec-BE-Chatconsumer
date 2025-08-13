package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.dto;

public record ChatRelayDto(
        Long senderId,
        Long receiverId,
        String content
) {
}
