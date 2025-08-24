package com.selfbell.user.domain;

import com.selfbell.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_rt_user", columnList = "user_id"),
        @Index(name = "idx_rt_jti", columnList = "jti", unique = true)
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RefreshToken extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 누가 썼는지 (회전/검증에 필요)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 실제 토큰 문자열(옵션: 해싱 저장도 가능)
    @Column(name = "token", nullable = false, length = 1024)
    private String token;

    // JWT jti 클레임 (고유 식별자)
    @Column(name = "jti", nullable = false, length = 64, unique = true)
    private String jti;

    // 만료시각(검증에 사용)
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    // 폐기 여부 (회전되거나 강제 로그아웃되면 true)
    @Column(name = "revoked", nullable = false)
    private boolean revoked;

    // 이 토큰이 무엇으로 교체되었는지(회전 추적)
    @Column(name = "replaced_by_jti", length = 64)
    private String replacedByJti;

    public void revoke(String replacedByJti) {
        this.revoked = true;
        this.replacedByJti = replacedByJti;
    }
}

