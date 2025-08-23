package com.selfbell.safewalk.repository;

import com.selfbell.safewalk.domain.SafeWalkGuardian;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SafeWalkGuardianRepository extends JpaRepository<SafeWalkGuardian, Long> {
    List<SafeWalkGuardian> findBySessionId(Long sessionId);

    @Query("SELECT g.session.id FROM SafeWalkGuardian g WHERE g.guardian.id = :guardianId")
    List<Long> findSessionIdByGuardianId(@Param("guardianId") Long guardianId);
}
