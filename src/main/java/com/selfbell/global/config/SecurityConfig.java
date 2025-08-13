package com.selfbell.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (API 서버/토큰 기반 전제)
                .csrf(AbstractHttpConfigurer::disable)

                // 세션은 사용하지 않음 (JWT 등 토큰 기반 전제)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 기본 인증/폼 로그인 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)

                // 엔드포인트 인가 정책
                .authorizeHttpRequests(auth -> auth
                        // 로그인, 회원가입 등 공개
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // 범죄자/안심벨 공개 API(기존 criminal 쪽 설정에서 모두 허용했던 부분 반영)
                        .requestMatchers("/api/v1/criminals/**").permitAll()
                        .requestMatchers("/api/v1/emergency-bells/**").permitAll()
                        // 헬스체크/운영(필요 시)
                        .requestMatchers("/actuator/**").permitAll()
                        // 그 외는 인증 필요
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
