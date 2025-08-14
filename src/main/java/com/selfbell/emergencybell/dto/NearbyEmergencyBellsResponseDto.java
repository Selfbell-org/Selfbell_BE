package com.selfbell.emergencybell.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class NearbyEmergencyBellsResponseDto {
    private int totalCount;
    private List<EmergencyBellSummaryDto> items;
}