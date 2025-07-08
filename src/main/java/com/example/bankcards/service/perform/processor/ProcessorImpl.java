package com.example.bankcards.service.perform.processor;

import com.example.bankcards.dto.perform.OperationType;
import com.example.bankcards.dto.perform.PerformRequest;
import com.example.bankcards.service.perform.strategy.PerformStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProcessorImpl implements Processor {
    private final Map<OperationType, PerformStrategy<?>> strategies;

    @Autowired
    public ProcessorImpl(List<PerformStrategy<?>> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(PerformStrategy::getOperationType, Function.identity()));
    }

    @Override
    public <T extends PerformRequest> void process(T performRequest, String username) {
        @SuppressWarnings("unchecked")
        PerformStrategy<T> performStrategy = (PerformStrategy<T>) strategies.get(performRequest.getOperationType());
        performStrategy.execute(performRequest, username);
    }
}
