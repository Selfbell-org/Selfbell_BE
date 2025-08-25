package com.selfbell.sos.dto;

import java.util.List;

public record SosSendRequest(
        List<Long> receiverUserIds,
        Long templateId,
        String message,
        Double lat,
        Double lon
) {
}
