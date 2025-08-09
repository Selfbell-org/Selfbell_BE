package com.selfbell.emergencybell.controller;

import com.selfbell.emergencybell.dto.EmergencyBellXmlDto;
import com.selfbell.emergencybell.service.EmergencyBellService;
import com.selfbell.emergencybell.domain.EmergencyBell;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/emergency-bells")
@RequiredArgsConstructor
public class EmergencyBellController {

    private final EmergencyBellService service;

    @GetMapping("/xml") //api 호출 -> xml형식으로 반환
    public EmergencyBellXmlDto getEmergencyBellsXml(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int numOfRows) throws Exception {
        return service.getEmergencyBellData(pageNo, numOfRows);
    }

    @GetMapping("/json") //api 호출 -> json형식으로 반환
    public String getEmergencyBellsJson(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int numOfRows) throws Exception {
        return service.getEmergencyBellDataAsJson(pageNo, numOfRows);
    }

    @GetMapping("/json-filtered") //필터링된 일부 필드만 확인
    public List<Map<String, Object>> getEmergencyBellsJsonFiltered(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int numOfRows) throws Exception {
        return service.getFilteredEmergencyBellData(pageNo, numOfRows);
    }

    @GetMapping("/update-db") //호출된 api를 db에 저장
    public String updateDb() throws Exception {
        EmergencyBellXmlDto dto = service.getEmergencyBellData(1, 100);
        service.saveOrUpdateEmergencyBells(dto.getBody().getItems().getItem());
        return "DB update complete";
    }

    @GetMapping("/nearby") //반경(단위m) 내
    public List<EmergencyBell> getNearbyEmergencyBells(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam double radius) {  // radius 단위: 미터

        return service.findNearbyEmergencyBells(lat, lon, radius);
    }
}
