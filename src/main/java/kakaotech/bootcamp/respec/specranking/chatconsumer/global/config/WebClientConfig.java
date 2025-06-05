package kakaotech.bootcamp.respec.specranking.chatconsumer.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient aiServerWebClient() {
        return WebClient.builder()
                .build();
    }
}
