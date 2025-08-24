package com.selfbell.safewalk.repository;

import com.selfbell.safewalk.domain.SafeWalkTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SafeWalkTrackRepository extends JpaRepository<SafeWalkTrack, Long> {
    @Query("SELECT t FROM SafeWalkTrack t WHERE t.session.id = :sessionId ORDER BY t.capturedAt ASC")
    List<SafeWalkTrack> findAllBySessionIdOrderByCapturedAtAsc(Long sessionId);
}
