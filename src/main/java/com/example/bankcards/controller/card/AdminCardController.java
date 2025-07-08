package com.example.bankcards.controller.card;

import com.example.bankcards.dto.card.CardCreateRequest;
import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.CardStatusUpdateRequest;
import com.example.bankcards.dto.card.PageResponse;
import com.example.bankcards.exception.validation.InvalidPageRequestException;
import com.example.bankcards.service.card.AdminCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import static net.logstash.logback.argument.StructuredArguments.keyValue;

@RestController
@RequestMapping("/cards/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminCardController {
    private final AdminCardService cardService;

    @PostMapping
    public ResponseEntity<CardDto> createCardForUser(@RequestBody @Valid CardCreateRequest request) {
        log.info("Create card for user",
                keyValue("for_user_id", request.getUserId())
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cardService.createCardForUser(request));
    }

    @GetMapping
    public ResponseEntity<PageResponse<CardDto>> getAllCards(@RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "100") int size) {
        log.info("Get all cards",
                keyValue("page", page),
                keyValue("size", size)
        );
        if (page < 0 || size <= 0) {
            throw new InvalidPageRequestException(
                    "Page index must not be less than zero, and size must be greater than zero."
            );
        }
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity
                .ok(cardService.getAllCards(pageable));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CardDto> updateCardStatus(@PathVariable Long id,
                                                    @RequestBody @Valid CardStatusUpdateRequest request) {
        log.info("Update card status",
                keyValue("card_id", id),
                keyValue("new_status", request.getStatus())
        );
        return ResponseEntity
                .ok(cardService.updateCardStatus(id, request.getStatus()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        log.info("Delete card",
                keyValue("card_id", id)
        );
        cardService.deleteCard(id);

        return ResponseEntity.noContent().build();
    }
}
