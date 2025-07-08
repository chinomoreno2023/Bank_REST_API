package com.example.bankcards.entity.auth;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "blacklisted_session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlacklistedSession {

    @Id
    private String sessionId;

    @Column(nullable = false)
    private Instant expiryDate;
}
