package com.selfbell.user.dto;

public record TokenRefreshResponse(
        String accessToken,
        String refreshToken,
        String tokenType,   // "Bearer"
        long   expiresIn    // seconds
) {}
