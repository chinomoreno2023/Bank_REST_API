package com.example.bankcards.dto.perform.transfer;

import com.example.bankcards.dto.perform.PerformRequest;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TransferRequest extends PerformRequest {

    @NotNull(message = "From card ID is required")
    private Long fromCardId;

    @NotNull(message = "To card ID is required")
    private Long toCardId;

    @NotNull(message = "Amount must be greater than 0")
    @DecimalMin(value = "0.01", inclusive = true)
    private BigDecimal amount;
}
