package com.selfbell.safewalk.controller;

import com.selfbell.safewalk.dto.*;
import com.selfbell.safewalk.service.SafeWalkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static com.selfbell.global.jwt.JwtTokenProvider.currentUserId;

@RestController
@RequestMapping("/api/v1/safe-walks")
@RequiredArgsConstructor
public class SafeWalkSessionController {

    private final SafeWalkService safeWalkService;

    @PostMapping
    public ResponseEntity<SessionCreateResponse> startSession(
            @RequestBody @Valid SessionCreateRequest request
    ) {
        Long userId = currentUserId();
        SessionCreateResponse response = safeWalkService.createSession(userId, request);
        return ResponseEntity.created(URI.create("/api/v1/safe-walks/" + response.sessionId())).body(response);
    }

    @PutMapping("/{sessionId}/end")
    public ResponseEntity<SessionEndResponse> endSession(
            @PathVariable Long sessionId,
            @RequestBody @Valid SessionEndRequest request
    ) {
        Long userId = currentUserId();
        SessionEndResponse response = safeWalkService.endSession(sessionId, userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<SessionDetailResponse> getSession(@PathVariable Long sessionId){
        Long userId = currentUserId();
        SessionDetailResponse response = safeWalkService.getSession(userId, sessionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ward/current")
    public ResponseEntity<SessionStatusResponse> getCurrentSessionStatus() {
        Long userId = currentUserId();
        return safeWalkService.getCurrentStatus(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/history")
    public ResponseEntity<SessionListResponse> getSessionList(
            @RequestParam String target){
        Long userId = currentUserId();
        SessionListResponse response = safeWalkService.getSessionList(userId, target);
        return ResponseEntity.ok(response);
    }
}
