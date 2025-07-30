package com.example.bankcards.config.kafka;

import com.example.bankcards.dto.event.Event;
import com.example.bankcards.dto.event.TransferEvent;
import com.example.bankcards.exception.event.NonRetryableException;
import com.example.bankcards.exception.event.RetryableException;
import com.example.bankcards.service.kafka.consumer.DLTHandler;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.RecordInterceptor;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.backoff.FixedBackOff;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class KafkaConfig {
    private static final String TRANSFER_TOPIC = "transfer-events-topic";
    private static final int PARTITIONS = 3;
    private static final int CONSUMER_CONCURRENCY = PARTITIONS;
    private static final int REPLICAS = 3;
    private static final String MIN_ISR = "2";

    @Value("${spring.kafka.producer.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.producer.key-serializer}")
    private String keySerializer;

    @Value("${spring.kafka.producer.value-serializer}")
    private String valueSerializer;

    @Value("${spring.kafka.producer.acks}")
    private String acks;

    @Value("${spring.kafka.producer.properties.delivery.timeout.ms}")
    private String deliveryTimeout;

    @Value("${spring.kafka.producer.properties.linger.ms}")
    private String linger;

    @Value("${spring.kafka.producer.properties.request.timeout.ms}")
    private String requestTimeout;

    @Value("${spring.kafka.producer.properties.enable.idempotence}")
    private boolean idempotence;

    @Value("${spring.kafka.producer.properties.max.in.flight.requests.per.connection}")
    private int inflightRequests;

    @Value("${spring.kafka.producer.transaction-id-prefix}")
    private String transactionalIdPrefix;

    @Value("${spring.kafka.consumer.bootstrap-servers}")
    private String consumerBootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String consumerGroupId;

    @Value("${spring.kafka.consumer.auto-offset-reset}")
    private String autoOffsetReset;

    @Value("${spring.kafka.consumer.properties.spring.json.trusted.packages}")
    private String trustedPackages;

    @Value("${spring.kafka.producer.properties.interceptor.classes}")
    private String interceptorClasses;

    @Value("${spring.kafka.properties.security.protocol}")
    private String securityProtocol;

    @Value("${spring.kafka.properties.ssl.endpoint.identification.algorithm}")
    private String sslEndpointIdentificationAlgorithm;

    @Value("${spring.kafka.properties.ssl.keystore.type}")
    private String sslKeystoreType;

    @Value("${spring.kafka.properties.ssl.truststore.type}")
    private String sslTruststoreType;

    @Value("${spring.kafka.properties.ssl.truststore.password}")
    private String sslTruststorePassword;

    @Value("${spring.kafka.properties.ssl.truststore.location}")
    private String sslTruststoreLocation;

    @Value("${spring.kafka.properties.ssl.keystore.location}")
    private String sslKeystoreLocation;

    @Value("${spring.kafka.properties.ssl.keystore.password}")
    private String sslKeystorePassword;

    @Value("${spring.kafka.properties.ssl.key.password}")
    private String sslKeyPassword;

    @Bean
    ConsumerFactory<String, Event> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, consumerBootstrapServers);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        config.put(JsonDeserializer.TRUSTED_PACKAGES, trustedPackages);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, Event.class.getName());
        config.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol);
        config.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, sslEndpointIdentificationAlgorithm);
        config.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, sslKeystoreType);
        config.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, sslTruststoreType);
        config.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, sslTruststorePassword);
        config.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, sslTruststoreLocation);
        config.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, sslKeystoreLocation);
        config.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, sslKeystorePassword);
        config.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, sslKeyPassword);
        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, Event> kafkaListenerContainerFactory(
            ConsumerFactory<String, Event> consumerFactory,
            @Qualifier("genericKafkaTemplate") KafkaTemplate<String, Event> kafkaTemplate,
            RecordInterceptor<String, Event> interceptor,
            DLTHandler dltHandler) {

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                recoverer(kafkaTemplate, dltHandler),
                new FixedBackOff(1000, 100));
        errorHandler.addNotRetryableExceptions(NonRetryableException.class);
        errorHandler.addRetryableExceptions(RetryableException.class);

        ConcurrentKafkaListenerContainerFactory<String, Event> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConcurrency(CONSUMER_CONCURRENCY);
        factory.setConsumerFactory(consumerFactory);
        factory.setRecordInterceptor(interceptor);
        factory.setCommonErrorHandler(errorHandler);
        factory.getContainerProperties().setListenerTaskExecutor(taskExecutor());
        return factory;
    }

    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CONSUMER_CONCURRENCY * PARTITIONS);
        executor.setMaxPoolSize(executor.getCorePoolSize() * 2);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("kafka-listener-");
        executor.setTaskDecorator(new MDCCopyingTaskDecorator());
        executor.initialize();
        return executor;
    }

    @Bean
    public DeadLetterPublishingRecoverer recoverer(
            @Qualifier("genericKafkaTemplate") KafkaTemplate<String, Event> kafkaTemplate,
            DLTHandler dltHandler) {
        return new DeadLetterPublishingRecoverer(kafkaTemplate, dltHandler::handle);
    }

    @Bean
    public RecordInterceptor<String, Event> mdcKafkaRecordInterceptor() {
        return new MdcKafkaRecordInterceptor();
    }

    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        props.put(ProducerConfig.ACKS_CONFIG, acks);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, deliveryTimeout);
        props.put(ProducerConfig.LINGER_MS_CONFIG, linger);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeout);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, idempotence);
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, inflightRequests);
        props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, transactionalIdPrefix);
        props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, interceptorClasses);
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol);
        props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, sslEndpointIdentificationAlgorithm);
        props.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, sslKeystoreType);
        props.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, sslTruststoreType);
        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, sslTruststorePassword);
        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, sslTruststoreLocation);
        props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, sslKeystoreLocation);
        props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, sslKeystorePassword);
        props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, sslKeyPassword);
        return props;
    }

    @Bean
    public <T extends Event> ProducerFactory<String, T> genericProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    @ConditionalOnMissingBean
    public <T extends Event> KafkaTemplate<String, T> genericKafkaTemplate() {
        return new KafkaTemplate<>(genericProducerFactory());
    }

    @Bean
    public KafkaTemplate<String, TransferEvent> transferKafkaTemplate() {
        return new KafkaTemplate<>(genericProducerFactory());
    }

    @Bean
    KafkaTransactionManager<String, Event> kafkaTransactionManager(
            ProducerFactory<String, Event> producerFactory) {
        return new KafkaTransactionManager<>(producerFactory);
    }

    @Bean(name = "transactionManager")
    @Primary
    JpaTransactionManager jpaTransactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    NewTopic transferEventsTopic() {
        return TopicBuilder
                .name(TRANSFER_TOPIC)
                .partitions(PARTITIONS)
                .replicas(REPLICAS)
                .configs(Map.of("min.insync.replicas", MIN_ISR))
                .build();
    }
}