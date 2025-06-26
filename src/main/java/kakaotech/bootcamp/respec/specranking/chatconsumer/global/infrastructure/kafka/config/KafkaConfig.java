package kakaotech.bootcamp.respec.specranking.chatconsumer.global.infrastructure.kafka.config;

import static kakaotech.bootcamp.respec.specranking.chatconsumer.global.infrastructure.kafka.constant.KafkaConfigConstant.CHAT_CONSUMER_GROUP;
import static kakaotech.bootcamp.respec.specranking.chatconsumer.global.infrastructure.kafka.constant.KafkaConfigConstant.CHAT_DLT_TOPIC;

import java.util.HashMap;
import java.util.Map;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.adapter.in.Event.ChatConsumeEvent;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.dto.ChatRelayDto;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

    private final KafkaProperties kafkaProperties;

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
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public ProducerFactory<String, ChatRelayDto> relayDltProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

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
                (r, e) -> new TopicPartition(CHAT_DLT_TOPIC, r.partition())
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
    public KafkaTemplate<String, ChatRelayDto> relayDltKafkaTemplate() {
        return new KafkaTemplate<>(relayDltProducerFactory());
    }

    private Map<String, Object> getConsumerProps() {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, CHAT_CONSUMER_GROUP);
        return consumerProps;
    }

    private static Map<String, Object> getDeserializerProps() {
        Map<String, Object> deserializerProps = new HashMap<>();
        deserializerProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        deserializerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, ChatConsumeEvent.class.getName());
        return deserializerProps;
    }

}
