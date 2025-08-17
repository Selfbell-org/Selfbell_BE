package com.selfbell.user.dto;

import com.selfbell.user.domain.User;
import lombok.Getter;

@Getter
public class UserSignupResponseDTO {

    private final Long userId;
    private final String name;
    private final String message;

    public UserSignupResponseDTO(Long userId, String name, String message) {
        this.userId = userId;
        this.name = name;
        this.message = message;
    }

    public static UserSignupResponseDTO from(User user) {
        return new UserSignupResponseDTO(user.getId(), user.getName(), "회원가입이 완료되었습니다.");
    }
}
