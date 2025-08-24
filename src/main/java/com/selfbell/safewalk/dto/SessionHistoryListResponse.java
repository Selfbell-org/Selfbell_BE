package com.selfbell.safewalk.dto;

import java.util.List;

public record SessionHistoryListResponse(
        List<SessionHistoryItem> sessions
) {
}
