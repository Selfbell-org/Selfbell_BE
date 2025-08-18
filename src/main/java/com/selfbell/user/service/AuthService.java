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

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO request) {
        String phoneNumber = request.getPhoneNumber();
        String password = request.getPassword();

        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "비밀번호가 일치하지 않습니다.");
        }

        // ★ phoneNumber를 subject로 발급
        String accessToken = jwtTokenProvider.createAccessToken(phoneNumber);
        String refreshToken = jwtTokenProvider.createRefreshToken(phoneNumber);

        return new LoginResponseDTO(accessToken, refreshToken);
    }
}
