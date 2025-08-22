package com.selfbell.user.service;

import com.selfbell.global.error.ApiException;
import com.selfbell.global.error.ErrorCode;
import com.selfbell.global.jwt.JwtTokenProvider;
import com.selfbell.user.domain.RefreshToken;
import com.selfbell.user.domain.User;
import com.selfbell.user.dto.TokenRefreshRequest;
import com.selfbell.user.dto.TokenRefreshResponse;
import com.selfbell.user.repository.RefreshTokenRepository;
import com.selfbell.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtTokenProvider jwt;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public TokenRefreshResponse refresh(TokenRefreshRequest request) {
        String incoming = request.refreshToken();

        if (!jwt.validateToken(incoming)) {
            try {
                jwt.getUserId(incoming);
                throw new ApiException(ErrorCode.REFRESH_TOKEN_INVALID);
            } catch (Exception e) {
                throw new ApiException(ErrorCode.REFRESH_TOKEN_EXPIRED);
            }
        }

        String typ = jwt.getType(incoming);
        if (!"refresh".equals(typ)) {
            throw new ApiException(ErrorCode.REFRESH_TOKEN_INVALID, "refresh 토큰이 아닙니다.");
        }

        Long userId = jwt.getUserId(incoming);
        String jti = jwt.getJti(incoming);
        Instant exp = jwt.getExpiryInstant(incoming);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        RefreshToken stored = refreshTokenRepository.findByJti(jti)
                .orElseThrow(() -> new ApiException(ErrorCode.REFRESH_TOKEN_INVALID));

        if (stored.isRevoked()) {
            throw new ApiException(ErrorCode.REFRESH_TOKEN_REUSED);
        }

        if (!stored.getToken().equals(incoming)) {
            throw new ApiException(ErrorCode.REFRESH_TOKEN_MISMATCH);
        }

        if (exp.isBefore(Instant.now())) {
            stored.revoke(null);
            refreshTokenRepository.save(stored);
            throw new ApiException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        String newAccess  = jwt.createAccessToken(user.getId());
        String newRefresh = jwt.createRefreshToken(user.getId());
        String newJti     = jwt.getJti(newRefresh);
        Instant newExp    = jwt.getExpiryInstant(newRefresh);

        stored.revoke(newJti);
        refreshTokenRepository.save(stored);

        RefreshToken rotated = RefreshToken.builder()
                .userId(user.getId())
                .token(newRefresh)
                .jti(newJti)
                .expiresAt(newExp)
                .revoked(false)
                .build();
        refreshTokenRepository.save(rotated);

        return new TokenRefreshResponse(
                newAccess,
                newRefresh,
                "Bearer",
                jwt.getAccessTokenValiditySeconds()
        );
    }

    @Transactional
    public void storeRefreshToken(Long userId, String refreshToken) {
        RefreshToken rt = RefreshToken.builder()
                .userId(userId)
                .token(refreshToken)
                .jti(jwt.getJti(refreshToken))
                .expiresAt(jwt.getExpiryInstant(refreshToken))
                .revoked(false)
                .build();
        refreshTokenRepository.save(rt);
    }

    @Transactional
    public void revokeRefreshByJti(String jti) {
        refreshTokenRepository.findByJti(jti).ifPresent(rt -> {
            rt.revoke(null);
            refreshTokenRepository.save(rt);
        });
    }
}
