package com.selfbell.safewalk.service;

import com.selfbell.safewalk.domain.GeoPoint;
import com.selfbell.safewalk.domain.SafeWalkSession;
import com.selfbell.safewalk.domain.enums.SafeWalkStatus;
import com.selfbell.safewalk.dto.Destination;
import com.selfbell.safewalk.dto.Origin;
import com.selfbell.safewalk.dto.SessionCreateRequest;
import com.selfbell.safewalk.dto.SessionCreateResponse;
import com.selfbell.safewalk.repository.SafeWalkSessionRepository;
import com.selfbell.user.domain.User;
import com.selfbell.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.selfbell.safewalk.domain.SafeWalkSession.createSafeWalkSession;

@Service
@RequiredArgsConstructor
public class SafeWalkService {

    private final SafeWalkSessionRepository safeWalkSessionRepository;

    private final UserService userService;

    @Transactional
    public SessionCreateResponse createSession(
            final Long userId,
            final SessionCreateRequest request
    ) {
        // TODO: 단일 진행중인 세션이 있는지 확인하는 로직 추가 필요
        final User user = userService.findByIdOrThrow(userId);
        final LocalDateTime now = LocalDateTime.now();

        final SafeWalkSession session = createSafeWalkSession(
                user,
                createGeoPointFromOrigin(request.origin()), request.originAddress(),
                createGeoPointFromDestination(request.destination()), request.destinationAddress(),
                parseExpectedArrival(request.expectedArrival()),
                calculateTimerEnd(request.timerMinutes(), now),
                now, null, SafeWalkStatus.IN_PROGRESS
                );

        safeWalkSessionRepository.save(session);

        // TODO: 세션 시작 시 트랙과 가디언도 생성
        // TODO: 알림 발송 로직 추가
        
        return SessionCreateResponse.from(session);
    }

    private GeoPoint createGeoPointFromOrigin(final Origin origin) {
        return GeoPoint.of(origin.lat(), origin.lon());
    }

    private GeoPoint createGeoPointFromDestination(final Destination destination) {
        return GeoPoint.of(destination.lat(), destination.lon());
    }

    private LocalDateTime parseExpectedArrival(final String expectedArrival) {
        if (expectedArrival == null || expectedArrival.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(expectedArrival);
    }

    private LocalDateTime calculateTimerEnd(final int timerMinutes, final LocalDateTime now) {
        if (timerMinutes <= 0) {
            return null;
        }
        return now.plusMinutes(timerMinutes);
    }
}
