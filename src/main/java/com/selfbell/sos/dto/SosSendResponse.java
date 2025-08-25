package com.selfbell.sos.dto;

import com.selfbell.sos.domain.SosMessage;
import lombok.Builder;

@Builder
public record SosSendResponse(
        Long id,
        Long senderId,
        Long templateId,
        String message,
        Double lat,
        Double lon,
        Long[] receivers,
        Integer sentCount,
        String createdAt
) {
    public static SosSendResponse from(SosMessage sosMessage, int sentCount) {
        return SosSendResponse.builder()
                .id(sosMessage.getId())
                .senderId(sosMessage.getSender().getId())
                .templateId(sosMessage.getTemplateId())
                .message(sosMessage.getMessage())
                .lat(sosMessage.getPoint().getLat().doubleValue())
                .lon(sosMessage.getPoint().getLon().doubleValue())
                .sentCount(sentCount)
                .createdAt(sosMessage.getCreatedAt().toString())
                .build();
    }
}
