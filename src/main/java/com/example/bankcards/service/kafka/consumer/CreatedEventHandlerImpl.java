package com.example.bankcards.service.kafka.consumer;

import com.example.bankcards.entity.card.Card;
import com.example.bankcards.dto.event.TransferEvent;
import com.example.bankcards.entity.event.ProcessedEventEntity;
import com.example.bankcards.exception.event.NonRetryableException;
import com.example.bankcards.exception.event.RetryableException;
import com.example.bankcards.repository.card.CardRepository;
import com.example.bankcards.repository.event.ProcessedEventRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import static net.logstash.logback.argument.StructuredArguments.keyValue;

@Slf4j
@Component
@KafkaListener(topics = "transfer-events-topic")
@RequiredArgsConstructor
public class CreatedEventHandlerImpl implements CreatedEventHandler<TransferEvent>{
    private final ProcessedEventRepository processedEventRepository;
    private final CardRepository cardRepository;

    @Override
    @KafkaHandler
    @Transactional(transactionManager = "transactionManager")
    public void handle(@Payload TransferEvent event, @Header("message_id") String messageId) {
        ProcessedEventEntity processedEventEntity = processedEventRepository.findByMessageId(messageId);

        if (processedEventEntity != null) {
            log.warn("Event already processed", keyValue("messageId", messageId));
            return;
        }

        Card from = cardRepository.findById(event.getFrom())
                .orElseThrow(() -> new EntityNotFoundException("Card not found"));
        Card to = cardRepository.findById(event.getTo())
                .orElseThrow(() -> new EntityNotFoundException("Card not found"));

        try {
            from.setBalance(from.getBalance().subtract(event.getAmount()));
            to.setBalance(to.getBalance().add(event.getAmount()));
        } catch (ResourceAccessException e) {
            throw new RetryableException("Network issue while saving wallet", e);
        } catch (Exception e) {
            throw new NonRetryableException("Unexpected error while saving wallet", e);
        }

        try {
            processedEventRepository.save(new ProcessedEventEntity(messageId, event.getEventId()));
        } catch (DataIntegrityViolationException e) {
            throw new NonRetryableException("Data integrity violation while saving processed event", e);
        }
    }
}
