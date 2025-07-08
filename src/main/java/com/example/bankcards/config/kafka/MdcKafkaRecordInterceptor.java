package com.example.bankcards.config.kafka;

import com.example.bankcards.dto.event.Event;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.MDC;
import org.springframework.kafka.listener.RecordInterceptor;
import java.nio.charset.StandardCharsets;
import java.util.List;
import static net.logstash.logback.argument.StructuredArguments.keyValue;

@Slf4j
public class MdcKafkaRecordInterceptor implements RecordInterceptor<String, Event> {
    private static final String REQUEST_ID = "request_id";
    private static final String AUTH_ACCOUNT = "auth_account";
    private static final String MESSAGE_ID = "message_id";
    private static final List<String> MDC_KEYS = List.of(REQUEST_ID, AUTH_ACCOUNT, MESSAGE_ID);

    @Override
    public ConsumerRecord<String, Event> intercept(ConsumerRecord<String, Event> record,
                                                    Consumer<String, Event> consumer) {
        try {
            for (String key : MDC_KEYS) {
                Header header = record.headers().lastHeader(key);
                if (header != null) {
                    String value = new String(header.value(), StandardCharsets.UTF_8);
                    MDC.put(key, value);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse Kafka header into MDC", keyValue("error", e.getMessage()), e);
        }
        return record;
    }

    @Override
    public void clearThreadState(Consumer<?, ?> consumer) {
        MDC.clear();
    }

    @Override
    public void failure(ConsumerRecord<String, Event> record,
                        Exception e,
                        Consumer<String, Event> consumer) {
        log.error("Kafka processing failed",
                keyValue("topic", record.topic()),
                keyValue("offset", record.offset()),
                keyValue("error", e.getMessage()),
                e
        );
    }
}
