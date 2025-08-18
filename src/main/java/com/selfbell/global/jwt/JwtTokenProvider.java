package com.selfbell.global.jwt;

import com.selfbell.global.error.ApiException;
import com.selfbell.global.error.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    private Key key;

    private final long ACCESS_TOKEN_VALIDITY = 1000L * 60 * 60;         // 1시간
    private final long REFRESH_TOKEN_VALIDITY = 1000L * 60 * 60 * 24 * 14; // 14일

    @PostConstruct
    public void init() {
        byte[] decodedKey = Base64.getDecoder().decode(secretKey);
        this.key = Keys.hmacShaKeyFor(decodedKey);
    }

    // ✅ 토큰 생성은 userId 기반
    public String createAccessToken(Long userId) {
        return createToken(userId, ACCESS_TOKEN_VALIDITY);
    }

    public String createRefreshToken(Long userId) {
        return createToken(userId, REFRESH_TOKEN_VALIDITY);
    }

    private String createToken(Long userId, long validity) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(String.valueOf(userId)) // ✅ sub = userId
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + validity))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ 토큰 → userId
    public Long getUserId(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return Long.parseLong(claims.getSubject());
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("만료된 JWT입니다.", e);
        } catch (JwtException | IllegalArgumentException e) {
            throw new RuntimeException("유효하지 않은 JWT입니다.", e);
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        return (bearerToken != null && bearerToken.startsWith("Bearer "))
                ? bearerToken.substring(7) : null;
    }

    public static Long currentUserId() {
        var a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated() || "anonymousUser".equals(String.valueOf(a.getPrincipal()))) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        }

        String userIdStr = a.getName(); // JWT subject = userId
        try {
            return Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "잘못된 인증 정보입니다.");
        }
    }
}
