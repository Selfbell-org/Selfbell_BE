package com.selfbell.criminal.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/** 주소 → 위경도(+ 사용자와의 거리 m) 응답 DTO */
@Getter
@Builder
public class CriminalCoordDto {
    private final String address;        // 예: "서울특별시 광진구 중곡동 18-109"
    private final BigDecimal latitude;   // 위도
    private final BigDecimal longitude;  // 경도
    private final long distanceMeters;   // 사용자와의 거리(m). nearby에서만 채워짐, 일반 coords는 0으로 둬도 OK
}
