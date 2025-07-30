package com.example.bankcards.config.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@ConfigurationProperties(prefix = "admin")
@Getter
@Setter
@Component
public class AdminProperties {
    private String username;
    private String password;
    private String firstCardNumber;
    private String secondCardNumber;
    private LocalDate initialCardsExpiration;
}
