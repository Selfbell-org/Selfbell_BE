package com.selfbell.safewalk.dto;

import com.selfbell.safewalk.domain.SafeWalkTrack;
import lombok.Builder;

@Builder
public record TrackResponse(
        Double lat,
        Double lon,
        Double accuracyM,
        String capturedAt
) {
    public static TrackResponse from(SafeWalkTrack track) {
        return TrackResponse.builder()
                .lat(track.getPoint().getLat().doubleValue())
                .lon(track.getPoint().getLon().doubleValue())
                .accuracyM(track.getAccuracyM())
                .capturedAt(track.getCapturedAt().toString())
                .build();
    }
}
