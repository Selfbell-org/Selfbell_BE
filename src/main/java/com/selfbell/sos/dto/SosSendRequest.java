package com.selfbell.sos.dto;

public record SosSendRequest(
        Long[] receiverUserIds,
        Long templateId,
        String message,
        Double lat,
        Double lon
) {
}
