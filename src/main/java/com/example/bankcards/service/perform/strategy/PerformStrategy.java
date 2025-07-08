package com.example.bankcards.service.perform.strategy;

import com.example.bankcards.dto.perform.OperationType;
import com.example.bankcards.dto.perform.PerformRequest;

public interface PerformStrategy<T extends PerformRequest> {
    void execute(T performRequest, String username);
    OperationType getOperationType();
}
