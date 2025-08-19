package com.selfbell.safewalk.repository;

import com.selfbell.safewalk.domain.SafeWalkSession;
import com.selfbell.safewalk.domain.enums.SafeWalkStatus;
import com.selfbell.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SafeWalkSessionRepository extends JpaRepository<SafeWalkSession, Long> {
    
    @Query("SELECT s FROM SafeWalkSession s WHERE s.ward = :ward AND s.safeWalkStatus = :status AND s.endedAt IS NULL")
    Optional<SafeWalkSession> findActiveSessionByWard(@Param("ward") User ward, @Param("status") SafeWalkStatus status);

    Optional<SafeWalkSession> findByWardIdAndSafeWalkStatus(Long userId, SafeWalkStatus status);
}
