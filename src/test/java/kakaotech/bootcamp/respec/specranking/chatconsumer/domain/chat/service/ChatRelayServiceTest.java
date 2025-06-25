package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.dto.ChatRelayDto;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@ExtendWith(MockitoExtension.class)
class ChatRelayServiceTest2 {

    private static MockWebServer mockWebServer;

    @InjectMocks
    private ChatRelayService chatRelayService;

    @Mock
    private WebClient webClient;

    @Mock
    private KafkaTemplate<String, ChatRelayDto> relayDltProducerFactory;

    private final String serverIp = "192.168.1.1:8080";
    private final Long senderId = 1L;
    private final Long receiverId = 2L;
    private final String CHAT_MESSAGE = "안녕하세요!";
    private final ChatRelayDto chatDto = new ChatRelayDto(senderId, receiverId, CHAT_MESSAGE);
    private final String WEB_CLIENT_ERROR = "WebClient error";

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("WebClient 호출 성공 시 DLQ로 전송하지 않는다")
    void whenRelaySucceeds_thenDlqNotCalled() {
        // given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("ok"));

        // when
        chatRelayService.relay(serverIp, chatDto);

        // then
        verify(relayDltProducerFactory, never()).send(any(), any(), any());
    }

    @Test
    @DisplayName("WebClient 호출 실패 시 DLQ로 전송된다")
    void whenRelayFails_thenSendToDlq() {
        // given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        // when
        chatRelayService.relay(serverIp, chatDto);

        // then
        verify(relayDltProducerFactory).send(anyString(), anyString(), eq(chatDto));
    }

}
