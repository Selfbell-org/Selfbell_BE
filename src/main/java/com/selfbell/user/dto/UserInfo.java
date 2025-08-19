package com.selfbell.user.dto;

import com.selfbell.user.domain.User;
import lombok.Builder;

@Builder
public record UserInfo(
        String name,
        String phoneNumber
) {
    public static UserInfo from(User user){
        return UserInfo.builder()
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }
}
