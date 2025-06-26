package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.service;

import static kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.constant.ChatRelayServiceConstant.CHAT_DLQ_TOPIC;
import static kakaotech.bootcamp.respec.specranking.chatconsumer.global.infrastructure.constant.UrlConstant.LOCALHOST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.dto.ChatRelayDto;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.fixture.ChatRelayFixture;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@ExtendWith(MockitoExtension.class)
class ChatRelayServiceTest {

    private final String VIRTUAL_SERVER_EXCEPTION_MESSAGE = "VIRTUAL_SERVER_EXCEPTION_MESSAGE";
    private final String VIRTUAL_SERVER_BODY_MESSAGE = "VIRTUAL_SERVER_BODY_MESSAGE";

    private final ChatRelayDto chatRelayDto = ChatRelayFixture.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private ChatRelayService chatRelayService;
    private MockWebServer mockWebServer;
    private KafkaTemplate<String, ChatRelayDto> relayDltProducerFactory;

    private String partnerServerIp;


    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        WebClient realWebClient = WebClient.builder().build();

        relayDltProducerFactory = Mockito.mock(KafkaTemplate.class);
        chatRelayService = new ChatRelayService(realWebClient, relayDltProducerFactory);
        partnerServerIp = LOCALHOST + ":" + mockWebServer.getPort();
    }

    @AfterEach
    void tearDownClass() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("성공적으로 메시지를 relay한다")
    void relaySuccess() throws Exception {
        // given
        mockWebServer.enqueue(new MockResponse()
                .setBody(VIRTUAL_SERVER_BODY_MESSAGE)
                .setResponseCode(HttpStatus.OK.value()));

        // when
        chatRelayService.relay(partnerServerIp, chatRelayDto);

        // then
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        String requestBody = recordedRequest.getBody().readUtf8();

        ChatRelayDto gottenJson = objectMapper.readValue(requestBody, ChatRelayDto.class);

        assertThat(gottenJson.senderId()).isEqualTo(chatRelayDto.senderId());
        assertThat(gottenJson.receiverId()).isEqualTo(chatRelayDto.receiverId());
        assertThat(gottenJson.content()).isEqualTo(chatRelayDto.content());
    }

    @Test
    @DisplayName("성공적으로 메시지를 릴레이 했을 경우 DLQ에 아무 것도 넣지 않는다.")
    void relay_Success_not_send_dlq() throws Exception {
        // given
        mockWebServer.enqueue(new MockResponse()
                .setBody(VIRTUAL_SERVER_BODY_MESSAGE)
                .addHeader("Content-Type", "application/json")
                .setResponseCode(HttpStatus.OK.value()));

        // when
        chatRelayService.relay(partnerServerIp, chatRelayDto);

        // then
        verify(relayDltProducerFactory, never()).send(any(), any(), any());
    }

    @Test
    @DisplayName("서버 에러 응답 시 최대 3회 retry 한다")
    void relay_Retry() throws Exception {
        // given
        for (int i = 0; i < 4; i++) {
            mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .setBody(VIRTUAL_SERVER_EXCEPTION_MESSAGE));
        }

        // when
        chatRelayService.relay(partnerServerIp, chatRelayDto);

        // then
        await().atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    int actualRequestCount = mockWebServer.getRequestCount();
                    assertThat(actualRequestCount).isEqualTo(3);
                });
    }

    @Test
    @DisplayName("retry 결과 모두 에러일 경우 DLQ에 넣는다.")
    void relay_Retry_ERROR_SEND_DLQ() throws Exception {
        // given
        for (int i = 0; i < 4; i++) {
            mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .setBody(VIRTUAL_SERVER_EXCEPTION_MESSAGE));
        }

        // when
        chatRelayService.relay(partnerServerIp, chatRelayDto);

        // then
        await().atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        verify(relayDltProducerFactory)
                                .send(eq(CHAT_DLQ_TOPIC), any(), eq(chatRelayDto))
                );
    }

    @Test
    @DisplayName("retry 3회 안에 정상 응답이 있을 경우, DLQ 전송하지 않는다.")
    void relay_Retry_Not_Dlq_Send() throws Exception {
        // given
        for (int i = 0; i < 2; i++) {
            mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .setBody(VIRTUAL_SERVER_EXCEPTION_MESSAGE));
        }
        mockWebServer.enqueue(
                new MockResponse().setBody(VIRTUAL_SERVER_BODY_MESSAGE).setResponseCode(HttpStatus.OK.value()));

        // when
        chatRelayService.relay(partnerServerIp, chatRelayDto);

        // then
        await().atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> verify(relayDltProducerFactory, never()).send(any(), any(), any()));
    }

}
