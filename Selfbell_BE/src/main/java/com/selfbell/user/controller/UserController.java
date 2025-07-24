package com.selfbell.user.controller;

import com.selfbell.user.dto.UserRequestDto;
import com.selfbell.user.dto.UserResponseDto;
import com.selfbell.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public UserResponseDto register(@RequestBody UserRequestDto requestDto) {
        return userService.saveUser(requestDto);
    }
}


