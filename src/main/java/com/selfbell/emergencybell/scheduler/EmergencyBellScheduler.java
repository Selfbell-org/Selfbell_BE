package com.selfbell.emergencybell.scheduler;

import com.selfbell.emergencybell.service.EmergencyBellService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmergencyBellScheduler {

    private final EmergencyBellService emergencyBellService;

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정 0시 0분 0초 실행
    public void updateEmergencyBellData() {
        try {
            // 1페이지에 100개씩 가져와서 저장 (필요시 조절 가능)
            var dto = emergencyBellService.getEmergencyBellData(1, 100);
            emergencyBellService.saveOrUpdateEmergencyBells(dto.getBody().getItems().getItem());

            log.info("안심벨 데이터 DB 자동 업데이트 완료 - {}", java.time.LocalDateTime.now());
        } catch (Exception e) {
            log.error("안심벨 데이터 업데이트 중 에러 발생: {}", e.getMessage(), e);
        }
    }
}
