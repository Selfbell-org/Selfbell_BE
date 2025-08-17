package com.selfbell.criminal.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/** 지번 기반 주소를 위도/경도로 변환한 결과 DTO */
@Getter
@Builder
public class CriminalCoordDto {
    private final String address;      // "서울특별시 광진구 중곡동 18-109" 등
    private final BigDecimal latitude; // 위도
    private final BigDecimal longitude;// 경도
}
