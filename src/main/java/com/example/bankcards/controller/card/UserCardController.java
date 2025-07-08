package com.example.bankcards.controller.card;

import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.PageResponse;
import com.example.bankcards.exception.validation.InvalidPageRequestException;
import com.example.bankcards.service.card.UserCardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import static net.logstash.logback.argument.StructuredArguments.*;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()")
public class UserCardController {
    private final UserCardService cardService;

    @GetMapping("/my")
    public ResponseEntity<List<CardDto>> getMyCards() {
        return ResponseEntity
                .ok(cardService.getMyCards());
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<BigDecimal> getMyCardBalance(@PathVariable Long id) {
        log.info("Get card balance",
                keyValue("card_id", id)
        );
        return ResponseEntity
                .ok(cardService.getMyCardBalance(id));
    }

    @GetMapping("/my/search")
    public ResponseEntity<PageResponse<CardDto>> getMyCards(@RequestParam(required = false) String number,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "10") int size) {
        log.info("Get my cards with filters",
                keyValue("number", number),
                keyValue("page", page),
                keyValue("size", size)
        );
        if (page < 0 || size <= 0) {
            throw new InvalidPageRequestException(
                    "Page index must not be less than zero, and size must be greater than zero."
            );
        }
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<CardDto> result = cardService.getMyCards(number, pageable);

        return ResponseEntity
                .ok(result);
    }

    @PostMapping("/{id}/block")
    public ResponseEntity<CardDto> requestMyCardBlock(@PathVariable Long id) {
        log.info("Request block card",
                keyValue("card_id", id)
        );
        return ResponseEntity
                .ok(cardService.requestMyCardBlock(id));
    }
}
