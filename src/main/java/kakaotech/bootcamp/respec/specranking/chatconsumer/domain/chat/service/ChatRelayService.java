package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.service;

import static kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.constant.ChatRelayServiceConstant.CHAT_DLQ_TOPIC;
import static kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.constant.ChatRelayServiceConstant.CHAT_RELAY_API_PATH;
import static kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.constant.ChatRelayServiceConstant.SCHEME;
import static kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.constant.ChatRelayServiceConstant.TOTAL_REQUEST_CNT;
import static kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.constant.ChatRelayServiceConstant.WAIT_MAX_SECONDS;

import java.time.Duration;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.dto.ChatRelayDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRelayService {

    private final KafkaTemplate<String, ChatRelayDto> relayDltProducerFactory;

    public void relay(String serverIp, ChatRelayDto dto) {
        String key = generateKeyForSequence(dto.senderId(), dto.receiverId());

        WebClient.builder().build()
                .post()
                .uri(SCHEME + serverIp + CHAT_RELAY_API_PATH)
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(WAIT_MAX_SECONDS))
                .retry(TOTAL_REQUEST_CNT - 1)
                .doOnError(error -> sendToDlq(key, dto))
                .subscribe();
    }

    private void sendToDlq(String key, ChatRelayDto dto) {
        relayDltProducerFactory.send(CHAT_DLQ_TOPIC, key, dto)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Kafka Relay FAILED topic=chat-dlq.relay key={} payload={} error={}", key, dto,
                                ex.getMessage(), ex);
                    }
                });
    }

    private String generateKeyForSequence(Long senderId, Long receiverId) {
        return (senderId < receiverId)
                ? senderId + "_" + receiverId
                : receiverId + "_" + senderId;
    }
}
