package com.selfbell.emergencybell.scheduler;

import com.selfbell.emergencybell.service.EmergencyBellService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmergencyBellScheduler {

    private final EmergencyBellService service;

    // application.yml에서 조정 가능 (기본: 자정에 증분)
    @Value("${emergencybell.scheduler.incremental-cron:0 0 0 * * *}")
    private String incrementalCron;

    // application.yml에서 조정 가능 (기본: 일요일 새벽 3시 전체)
    @Value("${emergencybell.scheduler.full-cron:0 0 3 * * SUN}")
    private String fullCron;

    /** 매일 증분 동기화 */
    @Scheduled(cron = "${emergencybell.scheduler.incremental-cron:0 0 0 * * *}")
    public void scheduledIncremental() {
        try {
            log.info("[스케줄] 증분 동기화 시작");
            service.incrementalSync();
            log.info("[스케줄] 증분 동기화 완료");
        } catch (Exception e) {
            log.error("[스케줄] 증분 동기화 실패: {}", e.getMessage(), e);
        }
    }

    /** 주 1회 전체 동기화 */
    @Scheduled(cron = "${emergencybell.scheduler.full-cron:0 0 3 * * SUN}")
    public void scheduledFull() {
        try {
            log.info("[스케줄] 전체 동기화 시작");
            service.fullSyncBulk();
            log.info("[스케줄] 전체 동기화 완료");
        } catch (Exception e) {
            log.error("[스케줄] 전체 동기화 실패: {}", e.getMessage(), e);
        }
    }
}
