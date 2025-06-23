package kakaotech.bootcamp.respec.specranking.chatconsumer.global.infrastructure.kafka.config;

import java.util.HashMap;
import java.util.Map;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.adapter.in.Event.ChatConsumeEvent;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
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
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

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
    public ConsumerFactory<String, ChatConsumeEvent> chatMessageConsumerFactory() {
        Map<String, Object> consumerProps = getConsumerProps();
        Map<String, Object> deserializerProps = getDeserializerProps();

        ErrorHandlingDeserializer<String> keyDeserializer =
                new ErrorHandlingDeserializer<>(new StringDeserializer());

        JsonDeserializer<ChatConsumeEvent> jsonDeserializer = new JsonDeserializer<>(ChatConsumeEvent.class);
        jsonDeserializer.configure(deserializerProps, false);

        ErrorHandlingDeserializer<ChatConsumeEvent> valueDeserializer =
                new ErrorHandlingDeserializer<>(jsonDeserializer);

        return new DefaultKafkaConsumerFactory<>(consumerProps, keyDeserializer, valueDeserializer);
    }

    @Bean
    public ProducerFactory<byte[], byte[]> dlqProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<byte[], byte[]> dlqProducerTemplate() {
        return new KafkaTemplate<>(dlqProducerFactory());
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                dlqProducerTemplate(),
                (r, e) -> new TopicPartition("chat-dlt", r.partition())
        );

        return new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3L));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ChatConsumeEvent> chatMessageContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ChatConsumeEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(chatMessageConsumerFactory());
        factory.setCommonErrorHandler(kafkaErrorHandler());
        return factory;
    }

    @Bean
    public NewTopic chatTopic() {
        return TopicBuilder.name("chat")
                .partitions(partitions_cnt)
                .replicas(replicas_cnt)
                .build();
    }

    @Bean
    public NewTopic chatDltTopic() {
        return TopicBuilder.name("chat-dlt")
                .partitions(partitions_cnt)
                .replicas(replicas_cnt)
                .build();
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
        deserializerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, ChatConsumeEvent.class.getName());
        return deserializerProps;
    }

}
