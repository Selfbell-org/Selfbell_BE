package com.selfbell.user.dto;

import com.selfbell.safewalk.domain.SafeWalkGuardian;
import com.selfbell.user.domain.User;
import lombok.Builder;

import java.util.List;

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

    public static List<UserResponse> fromList(List<SafeWalkGuardian> guardians) {
        return guardians.stream()
                .map(SafeWalkGuardian::getGuardian)
                .map(UserResponse::from)
                .toList();
    }
}
