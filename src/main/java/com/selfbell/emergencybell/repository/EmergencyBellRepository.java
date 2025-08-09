package com.selfbell.emergencybell.repository;

import com.selfbell.emergencybell.domain.EmergencyBell;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmergencyBellRepository extends JpaRepository<EmergencyBell, Long> {

    // 반경 검색용: 필요한 컬럼만 SELECT (nativeQuery)
    // 반환은 raw Object[] 리스트 -> Service에서 DTO로 매핑
    @Query(value = "SELECT " +
            "e.id, e.latitude, e.longitude, e.install_detail, e.management_phone, e.lot_number_address, e.install_type, e.coord_x, e.coord_y, " +
            "(6371000 * acos(cos(radians(:userLat)) * cos(radians(e.latitude)) * cos(radians(e.longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(e.latitude)))) AS distance " +
            "FROM emergency_bell e " +
            "HAVING distance <= :radiusInMeters " +
            "ORDER BY distance", nativeQuery = true)
    List<Object[]> findWithinRadiusRaw(
            @Param("userLat") double userLat,
            @Param("userLon") double userLon,
            @Param("radiusInMeters") double radiusInMeters);
}
