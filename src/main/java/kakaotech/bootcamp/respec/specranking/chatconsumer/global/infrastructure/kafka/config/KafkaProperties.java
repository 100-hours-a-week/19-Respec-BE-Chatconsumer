package kakaotech.bootcamp.respec.specranking.chatconsumer.global.infrastructure.kafka.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class KafkaProperties {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.chat.topic.partitions}")
    private int partitions_cnt;

    @Value("${spring.kafka.chat.topic.replicas}")
    private int replicas_cnt;
}
