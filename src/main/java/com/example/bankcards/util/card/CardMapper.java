package com.example.bankcards.util.card;

import com.example.bankcards.config.security.EncryptionProperties;
import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.entity.card.Card;
import com.example.bankcards.util.security.SymmetricEncryptor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CardMapper {
    private final SymmetricEncryptor encryptor;
    private final EncryptionProperties properties;

    public CardDto toMaskedResponse(Card card, String decryptedNumber) {
        return new CardDto(
                card.getId(),
                maskCardNumber(decryptedNumber),
                card.getOwner().getUsername(),
                card.getExpirationDate(),
                card.getStatus(),
                card.getBalance()
        );
    }

    public CardDto toResponse(Card card, String decryptedNumber) {
        return new CardDto(
                card.getId(),
                decryptedNumber,
                card.getOwner().getUsername(),
                card.getExpirationDate(),
                card.getStatus(),
                card.getBalance()
        );
    }

    public CardDto mapWithDecryption(Card card) {
        String decrypted = encryptor.decrypt(
                card.getEncryptedNumber(), properties.getCardKeyAlias()
        );
        return toMaskedResponse(card, decrypted);
    }

    public CardDto mapWithDecryptionNotMasked(Card card) {
        String decrypted = encryptor.decrypt(
                card.getEncryptedNumber(), properties.getCardKeyAlias()
        );
        return toResponse(card, decrypted);
    }

    private String maskCardNumber(String cardNumber) {
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}
