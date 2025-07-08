package com.example.bankcards.controller.perform.transfer;

import com.example.bankcards.dto.perform.transfer.TransferRequest;
import com.example.bankcards.service.perform.PerformService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static net.logstash.logback.argument.StructuredArguments.*;

@Slf4j
@RestController
@RequestMapping("/transfer")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class TransferController {
    private final PerformService<TransferRequest> transferService;

    @PostMapping
    public ResponseEntity<Void> perform(@RequestBody @Valid TransferRequest request) {
        log.info("Transfer request",
                keyValue("from_card_id", request.getFromCardId()),
                keyValue("to_card_id", request.getToCardId()),
                keyValue("amount", request.getAmount()),
                keyValue("operation_type", request.getOperationType())
        );
        transferService.perform(request);

        return ResponseEntity.noContent().build();
    }
}
