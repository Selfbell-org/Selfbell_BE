package com.selfbell.safewalk.repository;

import com.selfbell.safewalk.domain.SafeWalkSession;
import com.selfbell.safewalk.domain.enums.SafeWalkStatus;
import com.selfbell.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SafeWalkSessionRepository extends JpaRepository<SafeWalkSession, Long> {
    
    @Query("SELECT s FROM SafeWalkSession s WHERE s.ward = :ward AND s.safeWalkStatus = :status AND s.endedAt IS NULL")
    Optional<SafeWalkSession> findActiveSessionByWard(@Param("ward") User ward, @Param("status") SafeWalkStatus status);

    Optional<SafeWalkSession> findByWardIdAndSafeWalkStatus(Long userId, SafeWalkStatus status);

    List<SafeWalkSession> findByIdIn(List<Long> sessionIds);

    List<SafeWalkSession> findByWardId(Long userId);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
           "FROM SafeWalkSession s " +
           "WHERE s.id = :sessionId AND (s.ward.id = :userId OR " +
           "EXISTS (SELECT 1 FROM SafeWalkGuardian g WHERE g.session.id = s.id AND g.guardian.id = :userId))")
    boolean existsByIdAndWardIdOrGuardianId(@Param("sessionId") Long sessionId, @Param("userId") Long userId);
}
