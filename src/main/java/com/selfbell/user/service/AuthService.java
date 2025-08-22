package com.selfbell.user.service;

import com.selfbell.global.error.ApiException;
import com.selfbell.global.error.ErrorCode;
import com.selfbell.user.domain.User;
import com.selfbell.user.dto.LoginRequestDTO;
import com.selfbell.user.dto.LoginResponseDTO;
import com.selfbell.user.repository.UserRepository;
import com.selfbell.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public LoginResponseDTO login(LoginRequestDTO request) {
        String phoneNumber = request.getPhoneNumber();
        String password = request.getPassword();

        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        String jti = jwtTokenProvider.getJti(refreshToken);
        Instant exp = jwtTokenProvider.getExpiryInstant(refreshToken);
        refreshTokenService.store(user.getId(), refreshToken, jti, exp);

        long expiresInSec = jwtTokenProvider.getAccessTokenValiditySeconds();

        return new LoginResponseDTO(accessToken, refreshToken);
    }
}
