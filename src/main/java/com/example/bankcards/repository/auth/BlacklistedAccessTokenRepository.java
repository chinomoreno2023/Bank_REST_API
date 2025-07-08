package com.example.bankcards.repository.auth;

import com.example.bankcards.entity.auth.BlacklistedAccessToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface BlacklistedAccessTokenRepository extends JpaRepository<BlacklistedAccessToken, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<BlacklistedAccessToken> findByToken(String token);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM BlacklistedAccessToken t WHERE t.expiryDate < :now")
    List<BlacklistedAccessToken> findExpiredTokensForUpdate(Instant now);

}
