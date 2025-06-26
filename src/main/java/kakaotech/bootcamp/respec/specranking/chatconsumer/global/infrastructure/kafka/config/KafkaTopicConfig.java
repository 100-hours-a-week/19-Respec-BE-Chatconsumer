package kakaotech.bootcamp.respec.specranking.chatconsumer.global.infrastructure.kafka.config;

import static kakaotech.bootcamp.respec.specranking.chatconsumer.global.infrastructure.kafka.constant.KafkaConfigConstant.CHAT_DLT_RELAY_TOPIC;
import static kakaotech.bootcamp.respec.specranking.chatconsumer.global.infrastructure.kafka.constant.KafkaConfigConstant.CHAT_DLT_TOPIC;
import static kakaotech.bootcamp.respec.specranking.chatconsumer.global.infrastructure.kafka.constant.KafkaConfigConstant.CHAT_TOPIC;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@RequiredArgsConstructor
public class KafkaTopicConfig {

    private final KafkaProperties kafkaProperties;

    @Bean
    public NewTopic chatTopic() {
        return TopicBuilder.name(CHAT_TOPIC)
                .partitions(kafkaProperties.getPartitions_cnt())
                .replicas(kafkaProperties.getReplicas_cnt())
                .build();
    }

    @Bean
    public NewTopic chatDltTopic() {
        return TopicBuilder.name(CHAT_DLT_TOPIC)
                .partitions(kafkaProperties.getPartitions_cnt())
                .replicas(kafkaProperties.getReplicas_cnt())
                .build();
    }

    @Bean
    public NewTopic chatDltRelayTopic() {
        return TopicBuilder.name(CHAT_DLT_RELAY_TOPIC)
                .partitions(kafkaProperties.getPartitions_cnt())
                .replicas(kafkaProperties.getReplicas_cnt())
                .build();
    }
}
