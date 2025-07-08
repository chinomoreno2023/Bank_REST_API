package com.example.bankcards.dto.card;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CardCreateRequest {
    private Long userId;

    @Size(min = 16, max = 20, message = "Card number must be between 16 and 20 digits")
    @Pattern(regexp = "\\d+", message = "Card number must contain only digits")
    private String number;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Future(message = "Card expiration date must be in the future")
    private LocalDate validUntil;
}
