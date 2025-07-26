package com.selfbell.criminal.domain;

import com.selfbell.alert.domain.Alert;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.geo.Point;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Criminal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private int age;

    @Column(nullable = false, length = 100)
    private String address;

    @Column(nullable = false, length = 100)
    private String crimeType;

    @Column(nullable = false, columnDefinition = "geometry(Point, 4326)")
    private Point location;  // 위치 정보 (위도/경도 통합)
}
