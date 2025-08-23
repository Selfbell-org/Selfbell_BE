package com.selfbell.safewalk.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record SessionHistoryListResponse(
        List<SessionHistoryItem> sessions
) {
}
