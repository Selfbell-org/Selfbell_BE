package com.selfbell.safewalk.controller;

import com.selfbell.safewalk.dto.TrackListResponse;
import com.selfbell.safewalk.dto.TrackUploadRequest;
import com.selfbell.safewalk.dto.TrackUploadResponse;
import com.selfbell.safewalk.service.SafeWalkTrackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.selfbell.global.jwt.JwtTokenProvider.currentUserId;

@RestController
@RequestMapping("/api/v1/safe-walks")
@RequiredArgsConstructor
public class SafeWalkTrackController {

    private final SafeWalkTrackService safeWalkTrackService;

    @PostMapping("/{sessionId}/track")
    public ResponseEntity<TrackUploadResponse> uploadTrack(
            @PathVariable Long sessionId,
            @RequestBody @Valid TrackUploadRequest request
    ) {
        Long userId = currentUserId();
        TrackUploadResponse response = safeWalkTrackService.uploadTrack(sessionId, userId, request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{sessionId}/tracks")
    public ResponseEntity<TrackListResponse> retrieveTracks(
            @PathVariable Long sessionId
    ){
        Long userId = currentUserId();
        TrackListResponse response = safeWalkTrackService.retrieveTracks(sessionId, userId);
        return ResponseEntity.ok(response);
    }
}
