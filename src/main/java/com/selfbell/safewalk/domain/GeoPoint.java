package com.selfbell.safewalk.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Embeddable
@Getter @Setter
public class GeoPoint {
    @Column(precision = 10, scale = 7, nullable = false)
    private BigDecimal lat;

    @Column(precision = 10, scale = 7, nullable = false)
    private BigDecimal lon;

    public static GeoPoint of(double lat, double lon) {
        GeoPoint p = new GeoPoint();
        p.setLat(BigDecimal.valueOf(lat));
        p.setLon(BigDecimal.valueOf(lon));
        return p;
    }
}
