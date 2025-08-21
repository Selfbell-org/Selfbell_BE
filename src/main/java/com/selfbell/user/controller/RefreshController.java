package com.selfbell.user.controller;

import com.selfbell.global.error.ApiException;
import com.selfbell.global.error.ErrorCode;
import com.selfbell.global.jwt.JwtTokenProvider;
import com.selfbell.user.dto.LoginResponseDTO;
import com.selfbell.user.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class RefreshController {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refresh(HttpServletRequest request) {
        String refreshToken = jwtTokenProvider.resolveToken(request);
        if (refreshToken == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "리프레시 토큰이 필요합니다.");
        }
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다.");
        }

        Long userId = jwtTokenProvider.getUserId(refreshToken);
        String oldJti = jwtTokenProvider.getJti(refreshToken);

        if (!refreshTokenService.isValid(userId, oldJti)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "이미 사용되었거나 폐기된 리프레시 토큰입니다.");
        }

        String newAccess  = jwtTokenProvider.createAccessToken(userId);
        String newRefresh = jwtTokenProvider.createRefreshToken(userId);

        refreshTokenService.rotate(
                userId,
                oldJti,
                newRefresh,
                jwtTokenProvider.getJti(newRefresh),
                jwtTokenProvider.getExpiryInstant(newRefresh)
        );

        return ResponseEntity.ok(new LoginResponseDTO(newAccess, newRefresh));
    }
}
