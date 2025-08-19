package com.selfbell.safewalk.repository;

import com.selfbell.safewalk.domain.SafeWalkGuardian;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SafeWalkGuardianRepository extends JpaRepository<SafeWalkGuardian, Long> {
    List<SafeWalkGuardian> findBySessionId(Long sessionId);
}
