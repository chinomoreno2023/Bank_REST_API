package com.example.bankcards.service.kafka.producer.transfer;

import com.example.bankcards.dto.event.TransferEvent;
import com.example.bankcards.exception.event.EventSendException;
import com.example.bankcards.service.kafka.producer.ProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
@Service
@Slf4j
public class TransferProducerServiceImpl implements ProducerService<TransferEvent> {
    private final KafkaTemplate<String, TransferEvent> kafkaTemplate;
    private static final String RECORD_KEY_PREFIX = "key:";
    private static final String RECORD_KEY_SEPARATOR = ":";
    private static final int KEY_PREFIX_LENGTH = RECORD_KEY_PREFIX.length();
    private static final int SEPARATOR_LENGTH = RECORD_KEY_SEPARATOR.length();

    @Override
    public String createEvent(TransferEvent event) {
        String key = generateRecordKey(event);

        ProducerRecord<String, TransferEvent> record =
                new ProducerRecord<>("transfer-events-topic", key, event);

        try {
            kafkaTemplate.send(record).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EventSendException("Kafka producer was interrupted", e);
        } catch (ExecutionException e) {
            throw new EventSendException("Kafka producer failed", e);
        }
        return event.getEventId();
    }

    private String generateRecordKey(TransferEvent event) {
        long from = event.getFrom();
        long to = event.getTo();
        long min = Math.min(from, to);
        long max = Math.max(from, to);

        int estimatedSize
                = KEY_PREFIX_LENGTH
                + String.valueOf(min).length()
                + SEPARATOR_LENGTH
                + String.valueOf(max).length();

        return new StringBuilder(estimatedSize)
                .append(RECORD_KEY_PREFIX)
                .append(min)
                .append(RECORD_KEY_SEPARATOR)
                .append(max)
                .toString();
    }
}