package com.selfbell.user.controller;

import com.selfbell.user.dto.UserInfo;
import com.selfbell.user.dto.UserPhoneResponse;
import com.selfbell.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.selfbell.global.jwt.JwtTokenProvider.currentUserId;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserPhoneResponse> checkUserByPhoneNumber(
            @RequestParam String phoneNumber
    ) {
        UserPhoneResponse response = userService.checkUserByPhoneNumber(phoneNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserInfo> getUserInfo(){
        Long userId = currentUserId();
        UserInfo userInfo = userService.getUserInfo(userId);
        return ResponseEntity.ok(userInfo);
    }
}
