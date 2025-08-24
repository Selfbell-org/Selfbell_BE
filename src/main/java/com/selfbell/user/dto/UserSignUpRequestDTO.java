package com.selfbell.user.dto;

import com.selfbell.device.domain.enums.DeviceType;
import com.selfbell.user.domain.Role;
import com.selfbell.user.domain.User;
import com.selfbell.global.error.ApiException;
import com.selfbell.global.error.ErrorCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserSignUpRequestDTO {

    private String name;
    private String phoneNumber;
    private String password;

    // 선택 필드
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

    // deviceToken getter → blank 는 null 로 처리
    public String getDeviceToken() {
        return (deviceToken == null || deviceToken.isBlank()) ? null : deviceToken.trim();
    }

    // deviceType getter → blank 는 null 로 처리
    public String getDeviceType() {
        return (deviceType == null || deviceType.isBlank()) ? null : deviceType.trim();
    }

    // deviceType → DeviceType enum 변환 (null 허용)
    public DeviceType toDeviceTypeOrNull() {
        String type = getDeviceType();
        if (type == null) {
            return null;
        }
        try {
            return DeviceType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApiException(
                    ErrorCode.INVALID_INPUT,
                    "deviceType은 ANDROID 또는 IOS만 허용됩니다."
            );
        }
    }

    // deviceToken, deviceType 둘 다 있으면 true (둘 중 하나만 있으면 false)
    public boolean hasValidDeviceInfo() {
        return getDeviceToken() != null && getDeviceType() != null;
    }
}
