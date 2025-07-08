package kakaotech.bootcamp.respec.specranking.chatconsumer.global.infrastructure.kafka.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.adapter.in.kafka.Event.ChatConsumeEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
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
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@Slf4j
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
    public ProducerFactory<String, Object> objectDlqProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, Object> objectDlqTemplate() {
        return new KafkaTemplate<>(objectDlqProducerFactory());
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {
        return new DefaultErrorHandler((record, exception) -> {
            final String dlqTopic = "chat-dlt";
            final int partition = record.partition();

            if (record.key() instanceof byte[] && record.value() instanceof byte[]) {
                ProducerRecord<byte[], byte[]> outRecord = new ProducerRecord<>(
                        dlqTopic, partition, (byte[]) record.key(), (byte[]) record.value()
                );

                outRecord.headers().add("key-type", "byte[]".getBytes(StandardCharsets.UTF_8));
                outRecord.headers().add("value-type", "byte[]".getBytes(StandardCharsets.UTF_8));
                outRecord.headers().add("error-type", "DESERIALIZATION_ERROR".getBytes(StandardCharsets.UTF_8));

                dlqProducerTemplate().send(outRecord);
            } else if (record.key() instanceof String && record.value() instanceof ChatConsumeEvent) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    byte[] keyBytes = mapper.writeValueAsBytes(record.key());
                    byte[] valueBytes = mapper.writeValueAsBytes(record.value());
                    
                    ProducerRecord<byte[], byte[]> outRecord = new ProducerRecord<>(
                            dlqTopic, partition, keyBytes, valueBytes
                    );

                    outRecord.headers().add("key-type", "String".getBytes(StandardCharsets.UTF_8));
                    outRecord.headers().add("value-type", "ChatConsumeEvent".getBytes(StandardCharsets.UTF_8));
                    outRecord.headers().add("error-type", "RUNTIME_ERROR".getBytes(StandardCharsets.UTF_8));

                    dlqProducerTemplate().send(outRecord);
                } catch (Exception e) {
                    log.error("ChatConsumeEvent Byte 직렬화 과정에서 오류가 발생했습니다.", e);
                }
            } else {
                log.error("DefaultErrorHandler에 등록되지 않은 type이 Consume 되고, Runtime 에러 발생 했습니다.", exception);
            }
        }, new FixedBackOff(1000L, 3L));
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
