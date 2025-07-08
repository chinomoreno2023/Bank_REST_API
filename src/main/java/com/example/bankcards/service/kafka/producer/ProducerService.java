package com.example.bankcards.service.kafka.producer;

import com.example.bankcards.dto.event.Event;

public interface ProducerService<T extends Event> {
    String createEvent(T event);
}
