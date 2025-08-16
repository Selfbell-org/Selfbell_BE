package com.selfbell.global;

import com.selfbell.emergencybell.domain.EmergencyBell;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmergencyBellRepository extends JpaRepository<EmergencyBell, Long> {
}
