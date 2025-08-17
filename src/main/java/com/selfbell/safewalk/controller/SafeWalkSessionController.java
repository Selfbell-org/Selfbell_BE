package com.selfbell.safewalk.controller;

import com.selfbell.safewalk.dto.SessionCreateRequest;
import com.selfbell.safewalk.dto.SessionCreateResponse;
import com.selfbell.safewalk.service.SafeWalkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/safe-walks")
@RequiredArgsConstructor
public class SafeWalkSessionController {

    private final SafeWalkService safeWalkService;

    @PostMapping
    public ResponseEntity<SessionCreateResponse> startSession(
            Long userId,
            @RequestBody @Valid SessionCreateRequest request
    ) {
        SessionCreateResponse response = safeWalkService.createSession(userId, request);
        return ResponseEntity.created(URI.create("/api/safe-walks/" + response.sessionId())).body(response);
    }
}
