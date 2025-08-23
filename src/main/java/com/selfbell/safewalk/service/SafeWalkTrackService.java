package com.selfbell.safewalk.service;

import com.selfbell.safewalk.domain.GeoPoint;
import com.selfbell.safewalk.domain.SafeWalkSession;
import com.selfbell.safewalk.domain.SafeWalkTrack;
import com.selfbell.safewalk.dto.TrackListResponse;
import com.selfbell.safewalk.dto.TrackResponse;
import com.selfbell.safewalk.dto.TrackUploadRequest;
import com.selfbell.safewalk.dto.TrackUploadResponse;
import com.selfbell.safewalk.exception.SessionNotFoundException;
import com.selfbell.safewalk.repository.SafeWalkSessionRepository;
import com.selfbell.safewalk.repository.SafeWalkTrackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.selfbell.safewalk.service.SafeWalkService.validateSessionAccess;
import static com.selfbell.safewalk.service.SafeWalkService.validateSessionActive;

@Slf4j
@Service
@RequiredArgsConstructor
public class SafeWalkTrackService {

    private final SafeWalkTrackRepository safeWalkTrackRepository;
    private final SafeWalkSessionRepository safeWalkSessionRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public TrackUploadResponse uploadTrack(
            final Long sessionId,
            final Long userId,
            final TrackUploadRequest request
    ) {
        final SafeWalkSession session = findSessionByIdOrThrow(sessionId);
        
        validateSessionAccess(session, userId);
        validateSessionActive(session);
        
        final SafeWalkTrack track = createTrack(session, request);
        safeWalkTrackRepository.save(track);

         broadcastTrackEvent(track);
        
        log.info("트랙이 업로드되었습니다. 세션 ID: {}, 트랙 ID: {}, 사용자 ID: {}", 
            sessionId, track.getId(), userId);
        
        return TrackUploadResponse.from(track);
    }

    @Transactional(readOnly = true)
    public TrackListResponse retrieveTracks(Long sessionId) {
        findSessionByIdOrThrow(sessionId);

        final List<SafeWalkTrack> tracks = safeWalkTrackRepository.findAllBySessionIdOrderByCapturedAtAsc(sessionId);

        final List<TrackResponse> trackResponses = tracks.stream()
                .map(TrackResponse::from)
                .toList();

        return TrackListResponse.from(trackResponses);
    }

    private SafeWalkSession findSessionByIdOrThrow(final Long sessionId) {
        return safeWalkSessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));
    }


    private SafeWalkTrack createTrack(final SafeWalkSession session, final TrackUploadRequest request) {
        final GeoPoint point = GeoPoint.of(request.lat(), request.lon());
        final LocalDateTime capturedAt = LocalDateTime.parse(request.capturedAt());

        return SafeWalkTrack.createTrack(session, point, request.accuracyM(), capturedAt);
    }

    private void broadcastTrackEvent(final SafeWalkTrack track) {
        try {
            final String topic = "/topic/safe-walks/" + track.getSession().getId();
            final TrackUploadResponse response = TrackUploadResponse.from(track);

            messagingTemplate.convertAndSend(topic, response);

            log.debug("트랙 이벤트 브로드캐스트 완료. Topic: {}, 트랙 ID: {}", topic, track.getId());
        } catch (Exception e) {
            log.error("트랙 이벤트 브로드캐스트 실패. 트랙 ID: {}", track.getId(), e);
        }
    }
}
