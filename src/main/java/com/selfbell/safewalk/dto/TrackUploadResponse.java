package com.selfbell.safewalk.dto;

import com.selfbell.safewalk.domain.SafeWalkTrack;

public record TrackUploadResponse(
        Long trackId,
        Long sessionId,
        Double lat,
        Double lon,
        Double accuracyM,
        String capturedAt,
        String createdAt,
        String status
) {
    public static TrackUploadResponse from(SafeWalkTrack track) {
        return new TrackUploadResponse(
                track.getId(),
                track.getSession().getId(),
                track.getPoint().getLat().doubleValue(),
                track.getPoint().getLon().doubleValue(),
                track.getAccuracyM(),
                track.getCapturedAt().toString(),
                track.getCreatedAt().toString(),
                "UPLOADED"
        );
    }
}
