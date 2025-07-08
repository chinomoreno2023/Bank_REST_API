package com.accountapi.model.event;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "processed_events")
@NoArgsConstructor
@Getter
@Setter
public class ProcessedEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true)
    private String messageId;

    @Column(nullable = false)
    private String eventId;

    public ProcessedEventEntity(String messageId, String eventId) {
        this.messageId = messageId;
        this.eventId = eventId;
    }
}