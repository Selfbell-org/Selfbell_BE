package com.selfbell.user.dto;

import com.selfbell.user.domain.User;
import lombok.Getter;

@Getter
public class UserSignUpResponseDTO {

    private final Long userId;
    private final String name;
    private final String message;

    public UserSignUpResponseDTO(Long userId, String name, String message) {
        this.userId = userId;
        this.name = name;
        this.message = message;
    }

    public static UserSignUpResponseDTO from(User user) {
        return new UserSignUpResponseDTO(user.getId(), user.getName(), "회원가입이 완료되었습니다.");
    }
}
