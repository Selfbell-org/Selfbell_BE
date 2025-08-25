package com.selfbell.notification.dto;

import com.selfbell.notification.domain.enums.NotificationType;
import com.selfbell.sos.domain.SosMessage;
import lombok.Builder;

import java.util.HashMap;
import java.util.Map;

@Builder
public record SosNotification(
        String type,
        String sosMessageId,
        String senderId,
        String senderName,
        String senderPhone,
        String message,
        String lat,
        String lon,
        String createdAt,
        String notificationMessage
) {
    
    public static SosNotification createSosNotification(SosMessage sosMessage) {
        return SosNotification.builder()
                .type(NotificationType.SOS_MESSAGE.getCode())
                .sosMessageId(sosMessage.getId().toString())
                .senderId(sosMessage.getSender().getId().toString())
                .senderName(sosMessage.getSender().getName())
                .senderPhone(sosMessage.getSender().getPhoneNumber())
                .message(sosMessage.getMessage())
                .lat(sosMessage.getPoint().getLat().toString())
                .lon(sosMessage.getPoint().getLon().toString())
                .createdAt(sosMessage.getCreatedAt().toString())
                .notificationMessage(NotificationType.SOS_MESSAGE.getMessage())
                .build();
    }
    
    public Map<String, String> toDataMap() {
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("type", type);
        dataMap.put("sosMessageId", sosMessageId);
        dataMap.put("senderId", senderId);
        dataMap.put("senderName", senderName);
        dataMap.put("senderPhone", senderPhone);
        dataMap.put("message", message);
        dataMap.put("lat", lat);
        dataMap.put("lon", lon);
        dataMap.put("createdAt", createdAt);
        dataMap.put("notificationMessage", notificationMessage);
        
        return dataMap;
    }
}
