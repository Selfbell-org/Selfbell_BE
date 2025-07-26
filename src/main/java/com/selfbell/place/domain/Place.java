package com.selfbell.place.domain;

import com.selfbell.alert.domain.Alert;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "places")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String placeName;  // 이름 (집, 회사 등)

    @Column(name = "address", nullable = false, length = 100)
    private String address;  // 주소

    @Column(precision = 10, scale = 7)
    private Double latitude; // 위도 (nullable)

    @Column(precision = 10, scale = 7)
    private Double longitude; // 경도 (nullable)

    @Builder.Default
    @Min(100)
    @Max(1000)
    @Column(name = "radius_m", nullable = false, columnDefinition = "int default 500")
    private int radiusM = 500; // 반경 (기본값 500, 100~1000 사이 제약)

    @Builder.Default
    @Column(name = "alarm_on", nullable = false, columnDefinition = "boolean default true")
    private Boolean isOnAlarm = true; // 알림 활성화 여부 (기본값 true)

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Alert> alerts;
}
