package com.example.bankcards.entity.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import java.time.Instant;

@Entity
@Table(name = "blacklisted_access_tokens")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BlacklistedAccessToken extends Token {

    @Column(nullable = false)
    private Instant expiryDate;
}
