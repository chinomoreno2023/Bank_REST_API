package com.example.bankcards.repository.auth;

import com.example.bankcards.entity.auth.RefreshToken;
import com.example.bankcards.entity.user.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    List<RefreshToken> findByUser(User user);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<RefreshToken> findAllForUpdateByUser(User user);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RefreshToken> findByUserAndSessionId(User user, String sessionId);

    @Query("SELECT rt.tokenHash AS tokenHash, rt.tokenSalt AS tokenSalt, rt AS refreshToken " +
            "FROM RefreshToken rt " +
            "WHERE rt.user.id = :userId")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<RefreshTokenHashProjection> findHashesWithTokenByUser(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM RefreshToken t WHERE t.createdAt < :threshold")
    List<RefreshToken> findAllExpiredForUpdate(Instant threshold);
}