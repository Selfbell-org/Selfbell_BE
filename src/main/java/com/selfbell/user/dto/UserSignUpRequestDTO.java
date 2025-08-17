package com.selfbell.user.dto;

import com.selfbell.device.domain.enums.DeviceType;
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
                .build();
    }

    // deviceType → DeviceType enum 변환 (null/빈값 허용)
    public DeviceType toDeviceTypeOrNull() {
        if (deviceType == null || deviceType.isBlank()) {
            return null;
        }
        try {
            return DeviceType.valueOf(deviceType.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            // 잘못된 값이면 400 에러 던짐
            throw new ApiException(ErrorCode.INVALID_INPUT, "deviceType은 ANDROID 또는 IOS만 허용됩니다.");
        }
    }

    // deviceType, deviceToken 둘 다 들어왔는지 체크
    public boolean hasDeviceInfo() {
        return (deviceType != null && !deviceType.isBlank())
                || (deviceToken != null && !deviceToken.isBlank());
    }
}
