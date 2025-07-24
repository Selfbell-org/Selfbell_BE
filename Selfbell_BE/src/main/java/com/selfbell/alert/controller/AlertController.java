package com.selfbell.alert.controller;

import com.selfbell.alert.dto.AlertDto;
import com.selfbell.alert.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @PostMapping
    public ResponseEntity<AlertDto> create(@RequestBody AlertDto alertDto) {
        return ResponseEntity.ok(alertService.createAlert(alertDto));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AlertDto>> getAlertsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(alertService.getAlertsByUser(userId));
    }
}
