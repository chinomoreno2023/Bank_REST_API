package com.example.bankcards.entity.card;

import com.example.bankcards.entity.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "cards", indexes = {
        @Index(name = "idx_card_owner_last_four", columnList = "owner_id, lastFourDigits")
})
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String encryptedNumber;
    private LocalDate expirationDate;
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    private CardStatus status;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(name = "card_hash", nullable = false, unique = true)
    private String cardHash;

    @Column(name = "card_salt", nullable = false)
    private String cardSalt;

    @Column(name = "card_number_search_hash", nullable = false, unique = true)
    private String cardNumberSearchHash;

    @Column(name = "last_four_digits", nullable = false, length = 4)
    private String lastFourDigits;

    @Version
    private Long version;
}
