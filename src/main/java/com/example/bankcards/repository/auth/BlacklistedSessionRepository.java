package com.example.bankcards.repository.auth;

import com.example.bankcards.entity.auth.BlacklistedSession;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;

@Repository
public interface BlacklistedSessionRepository extends JpaRepository<BlacklistedSession, String> {
    boolean existsBySessionIdAndExpiryDateAfter(String sessionId, Instant now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM BlacklistedSession t WHERE t.expiryDate < :now")
    List<BlacklistedSession> findExpiredSessionsForUpdate(Instant now);
}