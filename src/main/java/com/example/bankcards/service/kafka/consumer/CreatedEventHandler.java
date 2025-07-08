package com.example.bankcards.service.kafka.consumer;

import com.example.bankcards.dto.event.Event;

public interface CreatedEventHandler<T extends Event> {
    void handle(T event, String messageId);
}
