package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.service;

import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.dto.ChatRelayDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class ChatRelayService {
    private final WebClient webClient;

    private static final String SCHEME = "http://";
    private static final String CHAT_RELAY_API_PATH = "/api/chat/relay";

    public void relay(String serverIp, ChatRelayDto dto) {
        webClient.post()
                .uri(SCHEME + serverIp + CHAT_RELAY_API_PATH)
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(String.class)
                .subscribe();
    }
}
