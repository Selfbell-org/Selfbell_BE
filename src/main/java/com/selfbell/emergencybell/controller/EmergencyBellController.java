package com.selfbell.emergencybell.controller;

import com.selfbell.emergencybell.dto.EmergencyBellSummaryDto;
import com.selfbell.emergencybell.dto.EmergencyBellXmlDto;
import com.selfbell.emergencybell.service.EmergencyBellService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/emergency-bells")
@RequiredArgsConstructor
public class EmergencyBellController {

    private final EmergencyBellService service;

    @GetMapping("/xml")
    public EmergencyBellXmlDto getEmergencyBellsXml(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int numOfRows) throws Exception {
        return service.getEmergencyBellData(pageNo, numOfRows);
    }

    @GetMapping("/json")
    public String getEmergencyBellsJson(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int numOfRows) throws Exception {
        return service.getEmergencyBellDataAsJson(pageNo, numOfRows);
    }

    @GetMapping("/json-filtered")
    public List<EmergencyBellSummaryDto> getEmergencyBellsJsonFiltered(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int numOfRows) throws Exception {
        return service.getFilteredEmergencyBellData(pageNo, numOfRows);
    }

    @GetMapping("/update-db") //api 호출 후 db 저장
    public String updateDb() throws Exception {
        var dto = service.getEmergencyBellData(1, 100);
        service.saveOrUpdateEmergencyBells(dto.getBody().getItems().getItem());
        return "DB update complete";
    }

    @GetMapping("/nearby")
    public List<EmergencyBellSummaryDto> getNearbyEmergencyBells(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam double radius) {
        return service.findNearbyEmergencyBells(lat, lon, radius);
    }
}
