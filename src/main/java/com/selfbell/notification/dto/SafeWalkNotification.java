package com.selfbell.notification.dto;

import com.selfbell.notification.domain.enums.NotificationType;
import com.selfbell.safewalk.domain.SafeWalkSession;
import lombok.Builder;

import java.util.HashMap;
import java.util.Map;

@Builder
public record SafeWalkNotification(
    String type,
    String sessionId,
    String wardName,
    String wardPhone,
    String originAddress,
    String destinationAddress,
    String destinationName,
    String expectedArrival,
    String timerEnd,
    String endedAt,
    String status,
    String message
) {
    
    public static SafeWalkNotification createStartedNotification(SafeWalkSession session) {
        return SafeWalkNotification.builder()
                .type(NotificationType.SAFE_WALK_STARTED.getCode())
                .sessionId(session.getId().toString())
                .wardName(session.getWard().getName())
                .wardPhone(session.getWard().getPhoneNumber())
                .originAddress(session.getOriginAddress())
                .destinationAddress(session.getDestinationAddress())
                .destinationName(session.getDestinationName())
                .expectedArrival(session.getExpectedArrival() != null ? session.getExpectedArrival().toString() : null)
                .timerEnd(session.getTimerEnd() != null ? session.getTimerEnd().toString() : null)
                .message(NotificationType.SAFE_WALK_STARTED.getMessage())
                .build();
    }
    
    public static SafeWalkNotification createEndedNotification(SafeWalkSession session) {
        return SafeWalkNotification.builder()
                .type(NotificationType.SAFE_WALK_ENDED.getCode())
                .sessionId(session.getId().toString())
                .wardName(session.getWard().getName())
                .wardPhone(session.getWard().getPhoneNumber())
                .originAddress(session.getOriginAddress())
                .destinationAddress(session.getDestinationAddress())
                .destinationName(session.getDestinationName())
                .endedAt(session.getEndedAt() != null ? session.getEndedAt().toString() : null)
                .status(session.getSafeWalkStatus().toString())
                .message(NotificationType.SAFE_WALK_ENDED.getMessage())
                .build();
    }
    
    public Map<String, String> toDataMap() {
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("type", type);
        dataMap.put("sessionId", sessionId);
        dataMap.put("wardName", wardName);
        dataMap.put("wardPhone", wardPhone);
        dataMap.put("originAddress", originAddress);
        dataMap.put("destinationAddress", destinationAddress);
        dataMap.put("destinationName", destinationName);
        dataMap.put("message", message);

        if (expectedArrival != null) {
            dataMap.put("expectedArrival", expectedArrival);
        }
        if (timerEnd != null) {
            dataMap.put("timerEnd", timerEnd);
        }
        if (endedAt != null) {
            dataMap.put("endedAt", endedAt);
        }
        if (status != null) {
            dataMap.put("status", status);
        }

        return dataMap;
    }
}
