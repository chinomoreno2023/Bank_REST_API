package com.example.bankcards.service.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.stereotype.Component;
import static net.logstash.logback.argument.StructuredArguments.keyValue;

@Component
@Slf4j
public class DLTHandler {
    public TopicPartition handle(ConsumerRecord<?, ?> record, Exception e) {
        log.error("Message sent to DLT",
                keyValue("topic", record.topic()),
                keyValue("offset", record.offset()),
                keyValue("error", e.getMessage()),
                e
        );
        return new TopicPartition(record.topic() + ".DLT", record.partition());
    }
}