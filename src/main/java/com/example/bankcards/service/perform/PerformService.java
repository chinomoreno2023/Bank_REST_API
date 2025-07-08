package com.example.bankcards.service.perform;

import com.example.bankcards.dto.perform.PerformRequest;

public interface PerformService<T extends PerformRequest> {
    void perform(T performRequest);
}