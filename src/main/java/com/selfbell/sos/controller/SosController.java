package com.selfbell.sos.controller;

import com.selfbell.sos.dto.SosSendRequest;
import com.selfbell.sos.dto.SosSendResponse;
import com.selfbell.sos.service.SosService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.selfbell.global.jwt.JwtTokenProvider.currentUserId;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sos/messages")
public class SosController {

    private final SosService sosService;

    @PostMapping
    public ResponseEntity<SosSendResponse> sendSos(@RequestBody SosSendRequest request) {
        Long userId = currentUserId();
        SosSendResponse response = sosService.sendSos(userId, request);
        return ResponseEntity.ok(response);
    }
}
