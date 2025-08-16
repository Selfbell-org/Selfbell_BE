package com.selfbell.safewalk.repository;

import com.selfbell.safewalk.domain.SafeWalkSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SafeWalkSessionRepository extends JpaRepository<SafeWalkSession, Long> {
}
