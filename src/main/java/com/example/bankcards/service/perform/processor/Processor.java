package com.example.bankcards.service.perform.processor;

import com.example.bankcards.dto.perform.PerformRequest;

public interface Processor {
    <T extends PerformRequest> void process(T performRequest, String username);
}
