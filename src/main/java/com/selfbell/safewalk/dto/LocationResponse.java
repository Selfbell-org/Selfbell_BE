package com.selfbell.safewalk.dto;

import com.selfbell.safewalk.domain.GeoPoint;
import lombok.Builder;

@Builder
public record LocationResponse(
        double lat,
        double lon,
        String addressText
) {
    public static LocationResponse from(GeoPoint point, String originAddress) {
        return LocationResponse.builder()
                .lat(point.getLat().doubleValue())
                .lon(point.getLon().doubleValue())
                .addressText(originAddress)
                .build();
    }
}
