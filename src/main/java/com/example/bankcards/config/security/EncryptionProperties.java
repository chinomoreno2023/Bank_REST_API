package com.example.bankcards.config.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "encryption")
@Getter
@Setter
public class EncryptionProperties {
    private String keystorePath;
    private String keystorePassword;
    private String jwtKeyAlias;
    private String cardKeyAlias;
    private String refreshKeyAlias;
    private long accessTokenExpiration;
    private long refreshTokenExpiration;
    private String pepper;
}
