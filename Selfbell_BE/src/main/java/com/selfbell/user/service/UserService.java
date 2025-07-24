package com.selfbell.user.service;

import com.selfbell.user.domain.User;
import com.selfbell.user.dto.UserRequestDto;
import com.selfbell.user.dto.UserResponseDto;
import com.selfbell.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponseDto saveUser(UserRequestDto dto) {
        User user = User.builder()
                .name(dto.getName())
                .phoneNumber(dto.getPhoneNumber())
                .password(dto.getPassword())  // 암호화는 추후 추가 가능
                .build();

        User saved = userRepository.save(user);

        return UserResponseDto.builder()
                .id(saved.getId())
                .name(saved.getName())
                .phoneNumber(saved.getPhoneNumber())
                .build();
    }
}

