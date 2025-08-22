package com.selfbell.user.service;

import com.selfbell.user.domain.RefreshToken;
import com.selfbell.user.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository repo;

    @Transactional
    public void store(Long userId, String token, String jti, Instant expiresAt) {
        repo.save(RefreshToken.builder()
                .userId(userId)
                .token(token)        // 필요 시 해싱해서 저장
                .jti(jti)
                .expiresAt(expiresAt)
                .revoked(false)
                .build());
    }

    @Transactional(readOnly = true)
    public boolean existsAndUsable(Long userId, String jti, Instant now) {
        return repo.findByJti(jti)
                .map(rt -> !rt.isRevoked()
                        && rt.getUserId().equals(userId)
                        && rt.getExpiresAt().isAfter(now))
                .orElse(false);
    }

    @Transactional
    public void revoke(String jti, String replacedByJti) {
        repo.findByJti(jti).ifPresent(rt -> rt.revoke(replacedByJti));
    }

    @Transactional
    public long cleanupExpired(Long userId, Instant before) {
        return repo.deleteByUserIdAndExpiresAtBefore(userId, before);
    }

    public boolean isValid(Long userId, String jti) {
        return repo.existsByUserIdAndJtiAndRevokedFalse(userId, jti);
    }

    public void rotate(Long userId, String oldJti, String newToken, String newJti, Instant newExpiresAt) {
        repo.findByUserIdAndJti(userId, oldJti).ifPresent(old -> {
            old.revoke(newJti);
            repo.save(old);
        });

        store(userId, newToken, newJti, newExpiresAt);
    }

}
