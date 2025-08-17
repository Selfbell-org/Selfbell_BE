package com.selfbell.safewalk.controller;

import com.selfbell.safewalk.dto.TrackUploadRequest;
import com.selfbell.safewalk.dto.TrackUploadResponse;
import com.selfbell.safewalk.service.SafeWalkTrackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/safe-walks")
@RequiredArgsConstructor
public class SafeWalkTrackController {

    private final SafeWalkTrackService safeWalkTrackService;

    @PostMapping("/{sessionId}/track")
    public ResponseEntity<TrackUploadResponse> uploadTrack(
            @PathVariable Long sessionId,
            @RequestParam Long userId,  // TODO: 인증 구현 후 @CurrentUser로 변경
            @RequestBody @Valid TrackUploadRequest request
    ) {
        TrackUploadResponse response = safeWalkTrackService.uploadTrack(sessionId, userId, request);

        return ResponseEntity.ok(response);
    }
}
