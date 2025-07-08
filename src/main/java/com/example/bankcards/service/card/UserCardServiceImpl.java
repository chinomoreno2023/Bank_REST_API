package com.example.bankcards.service.card;

import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.PageResponse;
import com.example.bankcards.entity.card.Card;
import com.example.bankcards.entity.card.CardStatus;
import com.example.bankcards.repository.card.CardRepository;
import com.example.bankcards.util.auth.ContextExtractor;
import com.example.bankcards.util.card.CardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import static com.example.bankcards.config.cache.RedisConfig.*;

@Service
@RequiredArgsConstructor
public class UserCardServiceImpl implements UserCardService {
    private final CardRepository cardRepository;
    private final CardChecker cardChecker;
    private final CardMapper cardMapper;
    private final ContextExtractor contextExtractor;

    @Override
    @Transactional(readOnly = true)
    public List<CardDto> getMyCards() {
        return cardRepository.findByOwnerId(contextExtractor.getUserFromContext().getId()).stream()
                .map(cardMapper::mapWithDecryptionNotMasked)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public BigDecimal getMyCardBalance(Long cardId) {
        Card card = cardChecker.getOwnedCard(cardId, contextExtractor.getUsernameFromContext());

        return card.getBalance();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = USER_CARDS_CACHE, keyGenerator = "userCardsCacheKeyGenerator")
    public PageResponse<CardDto> getMyCards(String numberFilter, Pageable pageable) {
        Long userId = contextExtractor.getUserFromContext().getId();
        Page<Card> cardsPage;

        if (numberFilter != null && !numberFilter.isBlank()) {
            cardsPage = cardRepository.findByOwnerIdAndLastFourDigitsContaining(userId, numberFilter, pageable);
        } else {
            cardsPage = cardRepository.findByOwnerId(userId, pageable);
        }
        Page<CardDto> dtoPage = cardsPage.map(cardMapper::mapWithDecryption);

        return PageResponse.fromEntity(dtoPage);
    }

    @Transactional
    @Override
    public CardDto requestMyCardBlock(Long cardId) {
        Card card = cardChecker.getOwnedCard(cardId, contextExtractor.getUsernameFromContext());
        card.setStatus(CardStatus.BLOCKED);

        return cardMapper.mapWithDecryption(cardRepository.save(card));
    }
}
