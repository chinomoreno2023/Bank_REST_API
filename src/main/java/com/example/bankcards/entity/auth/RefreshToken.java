package com.example.bankcards.entity.auth;

import com.example.bankcards.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.Instant;

@Entity
@Table(name = "refresh_token")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken extends Token {

    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id")
    private User user;

    @Column(nullable = false)
    private Instant expiryDate;

    @Column(nullable = false)
    private String ipAddress;

    @Column(nullable = true)
    private String userAgent;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private String sessionId;

    @Column(nullable = false, unique = true)
    private String tokenHash;

    @Column(nullable = false)
    private String tokenSalt;
}
