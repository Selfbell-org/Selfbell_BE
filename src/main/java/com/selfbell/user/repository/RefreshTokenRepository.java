package com.selfbell.user.repository;

import com.selfbell.user.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByJti(String jti);
    Optional<RefreshToken> findByUserIdAndJti(Long userId, String jti);
    long deleteByUserIdAndExpiresAtBefore(Long userId, Instant before);
    boolean existsByUserIdAndJtiAndRevokedFalse(Long userId, String jti);
}

