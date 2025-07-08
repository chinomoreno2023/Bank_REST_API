package com.example.bankcards.service.card;

import com.example.bankcards.entity.card.Card;
import com.example.bankcards.entity.card.CardStatus;
import com.example.bankcards.repository.card.CardRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CardChecker {
    private final CardRepository cardRepository;

    @Transactional(readOnly = true)
    public Card getOwnedCard(Long cardId, String username) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card not found"));

        if (!card.getOwner().getUsername().equals(username)) {
            throw new AccessDeniedException("Card does not belong to user");
        }
        return card;
    }

    @Transactional(readOnly = true)
    public void ensureActiveCardStatus(Card card) {
        if (!card.getStatus().equals(CardStatus.ACTIVE)) {
            throw new IllegalStateException("Card is not active");
        }
    }

    @Transactional(readOnly = true)
    public void ensureSufficientCardBalance(Card card, BigDecimal amount) {
        if (card.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance");
        }
    }
}
