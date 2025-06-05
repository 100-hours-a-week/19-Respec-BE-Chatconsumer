package kakaotech.bootcamp.respec.specranking.chatconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ChatconsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatconsumerApplication.class, args);
    }

}
