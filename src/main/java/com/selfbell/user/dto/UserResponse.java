package com.selfbell.user.dto;

import com.selfbell.user.domain.User;
import lombok.Builder;

@Builder
public record UserResponse(
        Long id,
        String name
) {
    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }
}
