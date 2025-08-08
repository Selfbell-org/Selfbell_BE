package com.selfbell.place.domain;

import com.selfbell.alert.domain.Alert;
import com.selfbell.user.domain.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;


import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "places")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name", nullable = false, length = 100)
    private String name;  // 이름 (예: 집, 회사)

    @Column(name = "address", nullable = false, length = 100)
    private String address;

    @Column(nullable = false)
    private BigDecimal latitude; // 위도

    @Column(nullable = false)
    private BigDecimal longitude; // 경도

    @Builder.Default
    @Min(100)
    @Max(1000)
    @Column(name = "radius_m", nullable = false, columnDefinition = "int default 500")
    private int radiusM = 500;

    @Builder.Default
    @Column(name = "alarm_on", nullable = false, columnDefinition = "boolean default true")
    private boolean alarmOn = true;

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Alert> alerts;
}