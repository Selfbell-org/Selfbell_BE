package com.selfbell.emergencybell.repository;

import com.selfbell.emergencybell.domain.EmergencyBell;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmergencyBellRepository extends JpaRepository<EmergencyBell, Long> {

    // 근처 조회
    @Query(value = """
        SELECT 
            eb.id,
            eb.lat,
            eb.lon,
            eb.ins_DETAIL,
            eb.mng_TEL,
            eb.adres,
            eb.ins_TYPE,
            ST_Distance_Sphere(POINT(:userLon, :userLat), POINT(eb.lon, eb.lat)) AS distance
        FROM emergency_bell eb
        WHERE ST_Distance_Sphere(POINT(:userLon, :userLat), POINT(eb.lon, eb.lat)) <= :radius
        ORDER BY distance ASC
        """, nativeQuery = true)
    List<Object[]> findWithinRadiusRaw(@Param("userLat") double userLat,
                                       @Param("userLon") double userLon,
                                       @Param("radius") double radiusInMeters);
}
