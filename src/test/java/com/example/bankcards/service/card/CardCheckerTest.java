package com.example.bankcards.service.card;

import com.example.bankcards.entity.card.Card;
import com.example.bankcards.entity.card.CardStatus;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.repository.card.CardRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardCheckerTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardChecker cardChecker;

    @Test
    void getOwnedCard_shouldReturnCard_whenOwnerMatches() {
        Long cardId = 1L;
        String username = "user@mail.ru";
        Card card = createCardWithOwner(username);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        Card result = cardChecker.getOwnedCard(cardId, username);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(card, result)
        );
    }

    @Test
    void getOwnedCard_shouldThrowEntityNotFoundException_whenCardNotFound() {
        Long cardId = 1L;
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        EntityNotFoundException e = assertThrows(EntityNotFoundException.class,
                () -> cardChecker.getOwnedCard(cardId, "invalid@mail.ru"));

        assertEquals("Card not found", e.getMessage());
    }

    @Test
    void getOwnedCard_shouldThrowAccessDeniedException_whenOwnerDoesNotMatch() {
        Long cardId = 1L;
        Card card = createCardWithOwner("invalid@mail.ru");
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        AccessDeniedException e = assertThrows(AccessDeniedException.class,
                () -> cardChecker.getOwnedCard(cardId, "user@mail.ru"));

        assertEquals("Card does not belong to user", e.getMessage());
    }

    @Test
    void ensureActiveCardStatus_shouldDoNothing_whenStatusIsActive() {
        Card card = new Card();
        card.setStatus(CardStatus.ACTIVE);

        assertDoesNotThrow(() -> cardChecker.ensureActiveCardStatus(card));
    }

    @Test
    void ensureActiveCardStatus_shouldThrowException_whenNotActive() {
        Card card = new Card();
        card.setStatus(CardStatus.BLOCKED);

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> cardChecker.ensureActiveCardStatus(card));

        assertEquals("Card is not active", e.getMessage());
    }

    @Test
    void ensureSufficientCardBalance_shouldDoNothing_whenEnoughBalance() {
        Card card = new Card();
        card.setBalance(new BigDecimal("100.00"));

        assertDoesNotThrow(() -> cardChecker.ensureSufficientCardBalance(card, new BigDecimal("99.99")));
    }

    @Test
    void ensureSufficientCardBalance_shouldThrowException_whenInsufficient() {
        Card card = new Card();
        card.setBalance(new BigDecimal("50.00"));

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> cardChecker.ensureSufficientCardBalance(card, new BigDecimal("100.00")));

        assertEquals("Insufficient balance", e.getMessage());
    }

    private Card createCardWithOwner(String username) {
        User user = new User();
        user.setUsername(username);
        Card card = new Card();
        card.setOwner(user);
        return card;
    }
}
