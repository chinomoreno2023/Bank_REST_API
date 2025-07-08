package com.example.bankcards.service.card;

import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.PageResponse;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;

public interface UserCardService {
    List<CardDto> getMyCards();
    PageResponse<CardDto> getMyCards(String numberFilter, Pageable pageable);
    BigDecimal getMyCardBalance(Long cardId);
    CardDto requestMyCardBlock(Long cardId);
}
