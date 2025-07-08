package com.example.bankcards.service.card;

import com.example.bankcards.dto.card.CardCreateRequest;
import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.PageResponse;
import com.example.bankcards.entity.card.CardStatus;
import org.springframework.data.domain.Pageable;

public interface AdminCardService {
    CardDto createCardForUser(CardCreateRequest request);
    PageResponse<CardDto> getAllCards(Pageable pageable);
    CardDto updateCardStatus(Long cardId, CardStatus status);
    void deleteCard(Long cardId);
}
