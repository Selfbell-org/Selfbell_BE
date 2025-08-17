package com.selfbell.user.controller;

import com.selfbell.user.domain.User;
import com.selfbell.user.dto.LoginRequestDTO;
import com.selfbell.user.dto.LoginResponseDTO;
import com.selfbell.user.dto.UserSignUpRequestDTO;
import com.selfbell.user.dto.UserSignUpResponseDTO;
import com.selfbell.user.service.AuthService;
import com.selfbell.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<UserSignupResponseDTO> signUp(@RequestBody @Valid UserSignUpRequestDTO request) {
        User user = userService.createUser(request);
        return ResponseEntity.status(201).body(UserSignUpResponseDTO.from(user));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
