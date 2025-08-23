package com.selfbell.safewalk.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record TrackListResponse(
        List<TrackResponse> items,
        int totalCount
) {
    public static TrackListResponse from(List<TrackResponse> items) {
        return TrackListResponse.builder()
                .items(items)
                .totalCount(items.size())
                .build();
    }
}
