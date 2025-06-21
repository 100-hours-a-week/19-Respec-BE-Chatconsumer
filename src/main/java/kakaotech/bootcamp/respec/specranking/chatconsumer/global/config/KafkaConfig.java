package kakaotech.bootcamp.respec.specranking.chatconsumer.global.config;

import java.util.HashMap;
import java.util.Map;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.dto.consume.ChatConsumeDto;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.chat.topic.partitions}")
    private int partitions_cnt;

    @Value("${spring.kafka.chat.topic.replicas}")
    private short replicas_cnt;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    @Bean
    public ProducerFactory<String, ChatConsumeDto> chatMessageProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, ChatConsumeDto> chatMessageKafkaProducerTemplate() {
        return new KafkaTemplate<>(chatMessageProducerFactory());
    }

    @Bean
    public ConsumerFactory<String, ChatConsumeDto> chatMessageConsumerFactory() {
        Map<String, Object> consumerProps = getConsumerProps();
        Map<String, Object> deserializerProps = getDeserializerProps();

        ErrorHandlingDeserializer<String> keyDeserializer =
                new ErrorHandlingDeserializer<>(new StringDeserializer());

        JsonDeserializer<ChatConsumeDto> jsonDeserializer = new JsonDeserializer<>(ChatConsumeDto.class);
        jsonDeserializer.configure(deserializerProps, false);

        ErrorHandlingDeserializer<ChatConsumeDto> valueDeserializer =
                new ErrorHandlingDeserializer<>(jsonDeserializer);

        return new DefaultKafkaConsumerFactory<>(consumerProps, keyDeserializer, valueDeserializer);


    }

    @Bean
    public NewTopic chatTopic() {
        return TopicBuilder.name("chat")
                .partitions(partitions_cnt)
                .replicas(replicas_cnt)
                .build();
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ChatConsumeDto> chatMessageContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ChatConsumeDto> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(chatMessageConsumerFactory());
        return factory;
    }

    private Map<String, Object> getConsumerProps() {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "chat-consumer-group");
        return consumerProps;
    }

    private static Map<String, Object> getDeserializerProps() {
        Map<String, Object> deserializerProps = new HashMap<>();
        deserializerProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        deserializerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, ChatConsumeDto.class.getName());
        return deserializerProps;
    }

}
