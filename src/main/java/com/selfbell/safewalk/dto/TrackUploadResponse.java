package com.selfbell.safewalk.dto;

import com.selfbell.safewalk.domain.SafeWalkTrack;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TrackUploadResponse(
        Long trackId,
        Long sessionId,
        Double lat,
        Double lon,
        Double accuracyM,
        LocalDateTime capturedAt,
        LocalDateTime createdAt,
        String status
) {
    public static TrackUploadResponse from(SafeWalkTrack track) {
        return new TrackUploadResponse(
                track.getId(),
                track.getSession().getId(),
                track.getPoint().getLat().doubleValue(),
                track.getPoint().getLon().doubleValue(),
                track.getAccuracyM(),
                track.getCapturedAt(),
                track.getCreatedAt(),
                "UPLOADED"
        );
    }
}
