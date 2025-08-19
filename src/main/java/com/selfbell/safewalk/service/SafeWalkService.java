package com.selfbell.safewalk.service;

import com.selfbell.safewalk.domain.GeoPoint;
import com.selfbell.safewalk.domain.SafeWalkGuardian;
import com.selfbell.safewalk.domain.SafeWalkSession;
import com.selfbell.safewalk.domain.enums.SafeWalkStatus;
import com.selfbell.safewalk.dto.*;
import com.selfbell.safewalk.exception.ActiveSessionExistsException;
import com.selfbell.safewalk.exception.SessionAccessDeniedException;
import com.selfbell.safewalk.exception.SessionNotActiveException;
import com.selfbell.safewalk.exception.SessionNotFoundException;
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
import java.util.Optional;

import static com.selfbell.safewalk.domain.SafeWalkGuardian.createGuardian;

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
                calculateTimerEnd(request.timerMinutes(), now), null, SafeWalkStatus.IN_PROGRESS
                );

        safeWalkSessionRepository.save(session);

        createGuardians(session, request.guardianIds());
        // TODO: 알림 서비스
        
        return SessionCreateResponse.from(session);
    }

    @Transactional
    public SessionEndResponse endSession(
            final Long sessionId,
            final Long userId,
            final SessionEndRequest request
    ) {
        SafeWalkSession session = findSessionByIdOrThrow(sessionId);
        final User user = userService.findByIdOrThrow(userId);
        validateSessionAccess(session, user.getId());
        validateSessionActive(session);

        // TODO: 세션 종료 이유에 따른 추가 로직 구현(현재는 단순히 세션 종료)
        session.endSession();
        // TODO: 알림 서비스

        return SessionEndResponse.of(session);
    }

    @Transactional(readOnly = true)
    public SessionResponse getSession(
            final Long userId,
            final Long sessionId
    ) {
        final SafeWalkSession session = findSessionByIdOrThrow(sessionId);
        validateSessionAccess(session, userId);
        validateSessionActive(session);

        final List<SafeWalkGuardian> guardians = safeWalkGuardianRepository.findBySessionId(sessionId);
        return SessionResponse.of(session, guardians);
    }

    @Transactional(readOnly = true)
    public Optional<SessionStatusResponse> getCurrentStatus(Long userId) {
        return safeWalkSessionRepository.findByWardIdAndSafeWalkStatus(userId, SafeWalkStatus.IN_PROGRESS)
                .map(SessionStatusResponse::from);
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

    private SafeWalkSession findSessionByIdOrThrow(final Long sessionId) {
        return safeWalkSessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));
    }

    public static void validateSessionAccess(final SafeWalkSession session, final Long userId) {
        if (!session.getWard().getId().equals(userId)) {
            log.warn("세션 접근 권한이 없습니다. 세션 ID: {}, 요청 사용자 ID: {}, 세션 소유자 ID: {}",
                    session.getId(), userId, session.getWard().getId());
            throw new SessionAccessDeniedException(session.getId(), userId);
        }
    }

    public static void validateSessionActive(final SafeWalkSession session) {
        if (!session.isActive()) {
            log.warn("비활성화된 세션에 트랙 업로드 | 조회 | 종료 시도. 세션 ID: {}, 상태: {}",
                    session.getId(), session.getSafeWalkStatus());
            throw new SessionNotActiveException(session.getId());
        }
    }
}
