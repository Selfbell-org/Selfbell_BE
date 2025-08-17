package com.selfbell.safewalk.service;

import com.selfbell.safewalk.domain.GeoPoint;
import com.selfbell.safewalk.domain.SafeWalkSession;
import com.selfbell.safewalk.domain.enums.SafeWalkStatus;
import com.selfbell.safewalk.dto.Destination;
import com.selfbell.safewalk.dto.Origin;
import com.selfbell.safewalk.dto.SessionCreateRequest;
import com.selfbell.safewalk.dto.SessionCreateResponse;
import com.selfbell.safewalk.domain.SafeWalkGuardian;
import com.selfbell.safewalk.exception.ActiveSessionExistsException;
import com.selfbell.safewalk.repository.SafeWalkGuardianRepository;
import com.selfbell.safewalk.repository.SafeWalkSessionRepository;
import com.selfbell.user.domain.User;
import com.selfbell.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.selfbell.safewalk.domain.SafeWalkGuardian.createGuardian;
import static com.selfbell.safewalk.domain.SafeWalkSession.createSafeWalkSession;

@Slf4j
@Service
@RequiredArgsConstructor
public class SafeWalkService {

    private final SafeWalkSessionRepository safeWalkSessionRepository;
    private final SafeWalkGuardianRepository safeWalkGuardianRepository;

    private final UserService userService;

    @Transactional
    public SessionCreateResponse createSession(
            final Long userId,
            final SessionCreateRequest request
    ) {
        final User user = userService.findByIdOrThrow(userId);
        validateNoActiveSession(user);

        final LocalDateTime now = LocalDateTime.now();

        final SafeWalkSession session = SafeWalkSession.createSession(
                user,
                createGeoPointFromOrigin(request.origin()), request.originAddress(),
                createGeoPointFromDestination(request.destination()), request.destinationAddress(),
                parseAndValidateExpectedArrival(request.expectedArrival()),
                calculateTimerEnd(request.timerMinutes(), now),
                now, null, SafeWalkStatus.IN_PROGRESS
                );

        safeWalkSessionRepository.save(session);

        createGuardians(session, request.guardianIds());
        // TODO: 알림 서비스
        
        return SessionCreateResponse.from(session);
    }


    private void validateNoActiveSession(User ward) {
        safeWalkSessionRepository.findActiveSessionByWard(ward, SafeWalkStatus.IN_PROGRESS)
                .ifPresent(session -> {
                    log.info("해당 유저의 이미 진행중인 세션이 있습니다. 세션 ID: {}", session.getId());
                    throw new ActiveSessionExistsException(session.getId());
                });
    }

    private GeoPoint createGeoPointFromOrigin(final Origin origin) {
        return GeoPoint.of(origin.lat(), origin.lon());
    }

    private GeoPoint createGeoPointFromDestination(final Destination destination) {
        return GeoPoint.of(destination.lat(), destination.lon());
    }

    private LocalDateTime parseAndValidateExpectedArrival(final String expectedArrival) {
        if (expectedArrival == null || expectedArrival.isEmpty()) {
            return null;
        }

        LocalDateTime parsedTime = LocalDateTime.parse(expectedArrival);

        // 도착 예정 시간이 과거인지 검증
        if (parsedTime.isBefore(LocalDateTime.now())) {
            log.warn("도착 예정 시간이 과거입니다. 입력값: {}", expectedArrival);
            throw new IllegalArgumentException("도착 예정 시간은 현재 시간보다 미래여야 합니다");
        }

        return parsedTime;
    }

    private LocalDateTime calculateTimerEnd(final Integer timerMinutes, final LocalDateTime now) {
        if (timerMinutes == null || timerMinutes <= 0) {
            return null;
        }
        return now.plusMinutes(timerMinutes);
    }

    private void createGuardians(final SafeWalkSession session, final List<Long> guardianIds) {
        if (guardianIds == null || guardianIds.isEmpty()) {
            log.info("보호자 리스트가 비어있습니다. 세션 ID: {}", session.getId());
            return;
        }

        final List<Long> distinctGuardians = guardianIds.stream().distinct().toList();

        final List<SafeWalkGuardian> safeWalkGuardianList = distinctGuardians.stream()
                .map(userService::findByIdOrThrow)
                .map(guardian -> createGuardian(session, guardian))
                .toList();

        safeWalkGuardianRepository.saveAll(safeWalkGuardianList);
        log.info("보호자 {}명을 세션에 추가했습니다. 세션 ID: {}", safeWalkGuardianList.size(), session.getId());
    }
}
