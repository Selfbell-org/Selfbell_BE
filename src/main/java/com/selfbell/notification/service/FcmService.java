package com.selfbell.notification.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.selfbell.device.domain.Device;
import com.selfbell.device.repository.DeviceRepository;
import com.selfbell.notification.dto.SafeWalkNotification;
import com.selfbell.safewalk.domain.SafeWalkSession;
import com.selfbell.safewalk.repository.SafeWalkGuardianRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.selfbell.notification.dto.SafeWalkNotification.createEndedNotification;
import static com.selfbell.notification.dto.SafeWalkNotification.createStartedNotification;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private final DeviceRepository deviceRepository;
    private final SafeWalkGuardianRepository safeWalkGuardianRepository;

    public void sendSafeWalkStartedNotification(SafeWalkSession session) {
        try {
            log.info("세션 시작 알림 전송 시작: sessionId={}", session.getId());
            
            SafeWalkNotification notificationData = createStartedNotification(session);
            List<Long> guardianIds = safeWalkGuardianRepository.findGuardianIdsBySessionId(session.getId());
            
            log.info("보호자 ID 목록: {}", guardianIds);
            
            if (guardianIds.isEmpty()) {
                log.warn("보호자가 없어서 알림을 전송하지 않습니다: sessionId={}", session.getId());
                return;
            }
            
            for (Long guardianId : guardianIds) {
                sendNotificationToUser(guardianId, notificationData);
            }
            
            log.info("세션 시작 알림 전송 완료: sessionId={}, guardianCount={}", session.getId(), guardianIds.size());
        } catch (Exception e) {
            log.error("세션 시작 알림 전송 실패: sessionId={}", session.getId(), e);
        }
    }

    public void sendSafeWalkEndedNotification(SafeWalkSession session) {
        try {
            SafeWalkNotification notificationData = createEndedNotification(session);
            List<Long> guardianIds = safeWalkGuardianRepository.findGuardianIdsBySessionId(session.getId());
            
            for (Long guardianId : guardianIds) {
                sendNotificationToUser(guardianId, notificationData);
            }
            
            log.info("세션 종료 알림 전송 완료: sessionId={}, guardianCount={}", session.getId(), guardianIds.size());
        } catch (Exception e) {
            log.error("세션 종료 알림 전송 실패: sessionId={}", session.getId(), e);
        }
    }

    private void sendNotificationToUser(Long userId, SafeWalkNotification notificationData) {
        try {
            log.info("개별 사용자 알림 전송 시도: userId={}", userId);
            
            if (FirebaseApp.getApps().isEmpty()) {
                log.warn("Firebase가 초기화되지 않아 알림을 전송할 수 없습니다: userId={}", userId);
                return;
            }
            
            Device device = deviceRepository.findByUserId(userId)
                    .orElse(null);
                    
            if (device == null) {
                log.warn("사용자 디바이스를 찾을 수 없습니다: userId={}", userId);
                return;
            }

            log.info("디바이스 토큰 발견: userId={}, token={}...", userId, 
                device.getDeviceToken().substring(0, Math.min(20, device.getDeviceToken().length())));

            Message message = Message.builder()
                    .setToken(device.getDeviceToken())
                    .putAllData(notificationData.toDataMap())
                    .build();

            log.info("FCM 메시지 전송 중: userId={}", userId);
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("FCM 알림 전송 성공: userId={}, type={}, response={}", userId, notificationData.type(), response);
            
        } catch (Exception e) {
            log.error("사용자 알림 전송 실패: userId={}, error={}", userId, e.getMessage(), e);
        }
    }
}
