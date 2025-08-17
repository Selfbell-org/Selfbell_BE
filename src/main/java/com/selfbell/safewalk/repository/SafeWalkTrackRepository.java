package com.selfbell.safewalk.repository;

import com.selfbell.safewalk.domain.SafeWalkTrack;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SafeWalkTrackRepository extends JpaRepository<SafeWalkTrack, Long> {
}
