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
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    private Key key;

    @Value("${jwt.access-token-validity}")
    private long ACCESS_TOKEN_VALIDITY;

    @Value("${jwt.refresh-token-validity}")
    private long REFRESH_TOKEN_VALIDITY;

    @PostConstruct
    public void init() {
        byte[] decodedKey = Base64.getDecoder().decode(secretKey);
        this.key = Keys.hmacShaKeyFor(decodedKey);
    }

    public String createAccessToken(Long userId) {
        return createToken(userId, ACCESS_TOKEN_VALIDITY, "access");
    }

    public String createRefreshToken(Long userId) {
        return createToken(userId, REFRESH_TOKEN_VALIDITY, "refresh");
    }

    private String createToken(Long userId, long validityMs, String type) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + validityMs);
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setSubject(userId.toString())
                .setId(jti)
                .claim("typ", type)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Long getUserId(String token) {
        try {
            Claims c = parseAllClaims(token);
            return Long.parseLong(c.getSubject());
        } catch (ExpiredJwtException e) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "만료된 토큰입니다.");
        } catch (JwtException | IllegalArgumentException e) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "유효하지 않은 토큰입니다.");
        }
    }

    public Long getUserIdAllowExpired(String token) {
        try {
            Claims c = parseAllClaimsAllowExpired(token);
            return Long.parseLong(c.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            throw new ApiException(ErrorCode.REFRESH_TOKEN_INVALID, "리프레시 토큰 파싱에 실패했습니다.");
        }
    }

    public String getType(String token) {
        try {
            Object v = parseAllClaimsAllowExpired(token).get("typ");
            return v == null ? null : v.toString();
        } catch (JwtException | IllegalArgumentException e) {
            throw new ApiException(ErrorCode.REFRESH_TOKEN_INVALID, "리프레시 토큰 typ 파싱 실패");
        }
    }

    public Instant getExpiryInstant(String token) {
        try {
            return parseAllClaimsAllowExpired(token).getExpiration().toInstant();
        } catch (JwtException | IllegalArgumentException e) {
            throw new ApiException(ErrorCode.REFRESH_TOKEN_INVALID, "리프레시 토큰 만료시각 파싱 실패");
        }
    }

    public boolean validateToken(String token) {
        try {
            parseAllClaims(token);  // 만료면 ExpiredJwtException 발생
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

    public long getAccessTokenValiditySeconds() {
        return ACCESS_TOKEN_VALIDITY / 1000L;
    }

    public static Long currentUserId() {
        var a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated() || "anonymousUser".equals(String.valueOf(a.getPrincipal()))) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        }
        try {
            return Long.parseLong(a.getName()); // subject = userId
        } catch (NumberFormatException e) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "잘못된 인증 정보입니다.");
        }
    }

    private Claims parseAllClaims(String token) {
        // 만료면 ExpiredJwtException 던짐
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();
    }

    private Claims parseAllClaimsAllowExpired(String token) {
        // 만료여도 클레임은 꺼내야 하는 경우(재발급)
        try {
            return Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims(); // 만료된 경우라도 클레임 반환
        }
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getJti(String token) {
        return parseClaims(token).getId();
    }

    public boolean isRefreshToken(String token) {
        Object typ = parseClaims(token).get("typ");
        return "refresh".equals(typ);
    }

    public boolean validateRefreshTokenFormat(String token) {
        try {
            if (!validateToken(token)) return false;           // 서명/만료 1차 검증
            if (!isRefreshToken(token)) return false;          // typ=refresh 인가
            String jti = getJti(token);
            return (jti != null && !jti.isBlank());            // jti 존재 여부
        } catch (Exception e) {
            return false;
        }
    }

    public Instant getExpirationInstant(String token) {
        return parseClaims(token).getExpiration().toInstant();
    }

}
