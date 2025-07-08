package com.example.bankcards.dto.card;

import com.example.bankcards.entity.card.CardStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CardStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private CardStatus status;
}
