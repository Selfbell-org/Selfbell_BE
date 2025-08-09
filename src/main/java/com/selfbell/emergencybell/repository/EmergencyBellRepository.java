package com.selfbell.emergencybell.repository;

import com.selfbell.emergencybell.domain.EmergencyBell;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmergencyBellRepository extends JpaRepository<EmergencyBell, Long> {

    @Query(value = "SELECT e.*, " +
            "(6371000 * acos(cos(radians(:userLat)) * cos(radians(e.latitude)) * cos(radians(e.longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(e.latitude)))) " +
            "AS distance " +
            "FROM emergency_bell e " +
            "HAVING distance <= :radiusInMeters " +
            "ORDER BY distance", nativeQuery = true)
    List<EmergencyBell> findWithinRadius(
            @Param("userLat") double userLat,
            @Param("userLon") double userLon,
            @Param("radiusInMeters") double radiusInMeters);
}
