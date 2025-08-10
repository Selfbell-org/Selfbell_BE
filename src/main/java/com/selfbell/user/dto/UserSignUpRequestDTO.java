package com.selfbell.user.dto;

import com.selfbell.device.domain.enums.DeviceType;
import com.selfbell.user.domain.Role;
import com.selfbell.user.domain.User;
import lombok.Getter;

@Getter
public class UserSignUpRequestDTO {

    private String name;
    private String phoneNumber;
    private String password;
    private String deviceToken;
    private String deviceType;

    public User toUser(String hashedPassword) {
        return User.builder()
                .name(name)
                .phoneNumber(phoneNumber)
                .password(hashedPassword)
                .role(Role.USER)
                .build();
    }

    public DeviceType toDeviceType() {
        return DeviceType.valueOf(deviceType);
    }
}