package com.example.bankcards.service.perform.transfer;

import com.example.bankcards.dto.perform.transfer.TransferRequest;
import com.example.bankcards.service.perform.PerformService;
import com.example.bankcards.service.perform.processor.Processor;
import com.example.bankcards.util.auth.ContextExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransferPerformServiceImpl implements PerformService<TransferRequest> {
    private final Processor processor;
    private final ContextExtractor contextExtractor;

    @Transactional
    public void perform(TransferRequest transferRequest) {
        String username = contextExtractor.getUsernameFromContext();
        processor.process(transferRequest, username);
    }
}
