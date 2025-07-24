package com.selfbell.device.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceResponseDto {
    private Long id;
    private Long userId;
    private String deviceToken;
    private String deviceType;
}
