package com.selfbell.emergencybell.controller;

import com.selfbell.emergencybell.dto.EmergencyBellSummaryDto;
import com.selfbell.emergencybell.dto.EmergencyBellXmlDto;
import com.selfbell.emergencybell.dto.NearbyEmergencyBellsResponseDto;
import com.selfbell.emergencybell.service.EmergencyBellService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/emergency-bells")
@RequiredArgsConstructor
public class EmergencyBellController {
    private final EmergencyBellService service;

    // XML 원본 조회
    @GetMapping("/xml")
    public EmergencyBellXmlDto getEmergencyBellsXml(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int numOfRows) throws Exception {
        return service.getEmergencyBellData(pageNo, numOfRows);
    }

    // JSON 직렬화
    @GetMapping("/json")
    public String getEmergencyBellsJson(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int numOfRows) throws Exception {
        return service.getEmergencyBellDataAsJson(pageNo, numOfRows);
    }

    // 필터링된 요약
    @GetMapping("/json-filtered")
    public List<EmergencyBellSummaryDto> getEmergencyBellsJsonFiltered(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int numOfRows) throws Exception {
        return service.getFilteredEmergencyBellData(pageNo, numOfRows);
    }

    // (테스트용) JPA 방식 upsert로 1페이지 저장
    @GetMapping("/update-db")
    public String updateDbJpa() throws Exception {
        var dto = service.getEmergencyBellData(1, 100);
        service.saveOrUpdateEmergencyBellsJpa(dto.getBody().getItems().getItem());
        return "DB update complete (JPA 1page)";
    }

    // 근처 안심벨 조회
    @GetMapping("/nearby")
    public NearbyEmergencyBellsResponseDto getNearbyEmergencyBells(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(required = false, defaultValue = "500") double radius) { //기본 반경 500 고정

        // 반경 값 검증
        if (radius < 100 || radius > 1000) {
            log.warn("반경 범위 초과: {}m (허용 범위: 100~1000m)", radius);
            throw new IllegalArgumentException("반경 범위는 100m 이상 1000m 이하만 가능합니다."); //예외처리-> 500 에러 던져줌
        }

        List<EmergencyBellSummaryDto> nearbyList = service.findNearbyEmergencyBells(lat, lon, radius);
        return new NearbyEmergencyBellsResponseDto(nearbyList.size(), nearbyList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmergencyBellSummaryDto> getEmergencyBellDetail(@PathVariable Long id) {
        return service.getEmergencyBellDetail(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "안심벨을 찾을 수 없습니다."));
    }


    // ===== 수동 트리거 엔드포인트 =====

    /** 최초 전체 적재(또는 강제 전체 재동기화) - JDBC Batch Upsert */
    @PostMapping("/full-sync")
    public String triggerFullSync() throws Exception {
        service.fullSyncBulk();
        return "Full sync (bulk upsert) completed";
    }

    /** 증분 동기화 수동 실행 */
    @PostMapping("/incremental-sync")
    public String triggerIncrementalSync() throws Exception {
        service.incrementalSync();
        return "Incremental sync (bulk upsert) completed";
    }
}
