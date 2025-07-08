package com.example.bankcards.dto.card;

import com.example.bankcards.entity.card.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class CardDto {
    private Long id;
    private String maskedNumber;
    private String ownerName;
    private LocalDate validUntil;
    private CardStatus status;
    private BigDecimal balance;
}
