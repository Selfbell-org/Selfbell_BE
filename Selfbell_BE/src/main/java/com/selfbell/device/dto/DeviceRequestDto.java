package com.selfbell.device.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceRequestDto {
    private Long userId;
    private String deviceToken;
    private String deviceType;
}

