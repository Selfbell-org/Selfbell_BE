package com.selfbell.emergencybell.repository;

import com.selfbell.emergencybell.domain.EmergencyBell;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmergencyBellRepository extends JpaRepository<EmergencyBell, Long> {
}

