package com.example.bankcards.config.kafka;

import com.example.bankcards.dto.event.Event;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Header;
import org.slf4j.MDC;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static net.logstash.logback.argument.StructuredArguments.keyValue;

@Slf4j
public class MdcKafkaProducerInterceptor implements ProducerInterceptor<String, Event> {
    private static final String REQUEST_ID = "request_id";
    private static final String AUTH_ACCOUNT = "auth_account";
    private static final String MESSAGE_ID = "message_id";
    private static final List<String> WHITELIST = List.of(REQUEST_ID, AUTH_ACCOUNT);

    @Override
    public ProducerRecord<String, Event> onSend(ProducerRecord<String, Event> record) {
        Map<String, String> mdc = MDC.getCopyOfContextMap();
        if (mdc != null) {
            WHITELIST.forEach(key -> {
                String value = mdc.get(key);
                if (value != null) {
                    record.headers().add(key, value.getBytes(StandardCharsets.UTF_8));
                }
            });
        }
        record.headers().add(MESSAGE_ID,
                UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));

        if (record.value() instanceof Event event) {
            log.info("Sending record to Kafka",
                    keyValue("topic", record.topic()),
                    keyValue("key", record.key()),
                    keyValue("eventId", event.getEventId()),
                    keyValue("message_id",
                            new String(record.headers().lastHeader(MESSAGE_ID).value(), StandardCharsets.UTF_8)
                    ),
                    keyValue("request_id", getHeaderValue(record, REQUEST_ID)),
                    keyValue("auth_account", getHeaderValue(record, AUTH_ACCOUNT)),
                    keyValue("message_size_bytes", record.value().toString().getBytes().length)
            );
        }
        return record;
    }

    private static String getHeaderValue(ProducerRecord<String, Event> record, String key) {
        Header header = record.headers().lastHeader(key);
        return header != null
                ? new String(header.value(), StandardCharsets.UTF_8)
                : "";
    }

    @Override
    public void onAcknowledgement(RecordMetadata recordMetadata, Exception e) {
        log.info("Record sent successfully to Kafka",
                keyValue("topic", recordMetadata.topic()),
                keyValue("partition", recordMetadata.partition()),
                keyValue("offset", recordMetadata.offset())
        );
    }

    @Override
    public void close() {}

    @Override
    public void configure(Map<String, ?> map) {}
}
