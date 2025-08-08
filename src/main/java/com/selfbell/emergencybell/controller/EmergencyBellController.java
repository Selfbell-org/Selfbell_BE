package com.selfbell.emergencybell.controller;

import com.selfbell.emergencybell.dto.EmergencyBellXmlDto;
import com.selfbell.emergencybell.service.EmergencyBellService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
}
