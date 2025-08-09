package com.selfbell.emergencybell.repository;

import com.selfbell.emergencybell.domain.EmergencyBell;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmergencyBellRepository extends JpaRepository<EmergencyBell, Long> {

    @Query(value = "SELECT * FROM (" +
            "SELECT e.id, e.lat, e.lon, e.ins_DETAIL, e.mng_TEL, e.adres, e.ins_TYPE, e.x, e.y, " +
            "(6371000 * acos(cos(radians(:userLat)) * cos(radians(e.lat)) * cos(radians(e.lon) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(e.lat)))) AS distance " +
            "FROM emergency_bell e " +
            ") AS distances " +
            "WHERE distance <= :radiusInMeters " +
            "ORDER BY distance", nativeQuery = true)
    List<Object[]> findWithinRadiusRaw(@Param("userLat") double userLat,
                                       @Param("userLon") double userLon,
                                       @Param("radiusInMeters") double radiusInMeters);
}

