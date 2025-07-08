package com.example.bankcards.service.perform.strategy;

import com.example.bankcards.dto.perform.OperationType;
import com.example.bankcards.dto.perform.transfer.TransferRequest;
import com.example.bankcards.entity.card.Card;
import com.example.bankcards.dto.event.TransferEvent;
import com.example.bankcards.service.card.CardChecker;
import com.example.bankcards.service.kafka.producer.ProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferPerformStrategyImpl implements PerformStrategy<TransferRequest> {
    private final CardChecker cardChecker;
    private final ProducerService<TransferEvent> producerService;

    @Override
    public void execute(TransferRequest performRequest, String username) {
        if (performRequest.getFromCardId().equals(performRequest.getToCardId())) {
            throw new IllegalArgumentException("You cannot transfer to the same card");
        }

        Card from = cardChecker.getOwnedCard(performRequest.getFromCardId(), username);
        Card to = cardChecker.getOwnedCard(performRequest.getToCardId(), username);
        BigDecimal amount = performRequest.getAmount();

        cardChecker.ensureActiveCardStatus(from);
        cardChecker.ensureActiveCardStatus(to);
        cardChecker.ensureSufficientCardBalance(from, amount);

        producerService.createEvent(TransferEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .from(from.getId())
                .to(to.getId())
                .amount(amount)
                .build());
    }

    @Override
    public OperationType getOperationType() {
        return OperationType.TRANSFER;
    }
}
