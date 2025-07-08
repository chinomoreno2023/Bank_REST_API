package com.example.bankcards.service.card;

import com.example.bankcards.config.security.EncryptionProperties;
import com.example.bankcards.dto.card.CardCreateRequest;
import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.PageResponse;
import com.example.bankcards.entity.card.Card;
import com.example.bankcards.entity.card.CardStatus;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.exception.validation.CardAlreadyExistsException;
import com.example.bankcards.repository.card.CardRepository;
import com.example.bankcards.repository.user.UserRepository;
import com.example.bankcards.security.SaltByteLength;
import com.example.bankcards.service.cache.RedisCacheServiceImpl;
import com.example.bankcards.util.card.CardMapper;
import com.example.bankcards.util.security.SymmetricEncryptor;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import static com.example.bankcards.config.cache.RedisConfig.ADMIN_CARDS_CACHE;
import static com.example.bankcards.security.HashAlgorithm.SHA_256;
import static com.example.bankcards.util.security.HashUtil.generateSalt;
import static com.example.bankcards.util.security.HashUtil.hashWithSecret;

@Component
@RequiredArgsConstructor
public class AdminCardServiceImpl implements AdminCardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final SymmetricEncryptor symmetricEncryptor;
    private final CardMapper cardMapper;
    private final EncryptionProperties encryptionProperties;
    private final RedisCacheServiceImpl redisCacheEvictionService;

    @Override
    @Transactional
    public CardDto createCardForUser(CardCreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        String cardNumber = request.getNumber();

        String searchHash = hashWithSecret(cardNumber, encryptionProperties.getPepper(), SHA_256);
        String salt = generateSalt(SaltByteLength.SALT_16);
        String hash = hashWithSecret(request.getNumber(), salt, SHA_256);

        String encryptedNumber = symmetricEncryptor.encrypt(
                request.getNumber(), encryptionProperties.getCardKeyAlias()
        );
        Card card = new Card();
        card.setOwner(user);
        card.setEncryptedNumber(encryptedNumber);
        card.setExpirationDate(request.getValidUntil());
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.ZERO);
        card.setCardHash(hash);
        card.setCardSalt(salt);
        card.setCardNumberSearchHash(searchHash);
        card.setLastFourDigits(cardNumber.substring(cardNumber.length() - 4));

        try {
            Card savedCard = cardRepository.save(card);
            redisCacheEvictionService.evictUserCardsCache(request.getUserId());
            redisCacheEvictionService.evictAdminCardsCache();
            return cardMapper.toMaskedResponse(savedCard, cardNumber);
        } catch (DataIntegrityViolationException e) {
            throw new CardAlreadyExistsException("Card with this number already exists.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = ADMIN_CARDS_CACHE, keyGenerator = "adminCardsCacheKeyGenerator")
    public PageResponse<CardDto> getAllCards(Pageable pageable) {
        Page<CardDto> dtoPage = cardRepository.findAll(pageable)
                .map(cardMapper::mapWithDecryptionNotMasked);

        return PageResponse.fromEntity(dtoPage);
    }

    @Override
    @Transactional
    public CardDto updateCardStatus(Long cardId, CardStatus status) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card not found"));

        if (card.getStatus().equals(status)) {
            throw new IllegalStateException("Card status is already " + status);
        }
        card.setStatus(status);

        return cardMapper.mapWithDecryption(cardRepository.save(card));
    }

    @Override
    @Transactional
    public void deleteCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card not found"));

        cardRepository.delete(card);

        redisCacheEvictionService.evictUserCardsCache(card.getOwner().getId());
        redisCacheEvictionService.evictAdminCardsCache();
    }
}
