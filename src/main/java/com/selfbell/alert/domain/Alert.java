package com.selfbell.alert.domain;

import com.selfbell.criminal.domain.Criminal;
import com.selfbell.place.domain.Place;
import com.selfbell.user.domain.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;

    private LocalDateTime sentAt;

    @Builder.Default
    @Min(100)
    @Max(1000)
    @Column(nullable = false, columnDefinition = "int default 500") //기본 500
    private int radiusM = 500; // 알림 반경 (100 ~ 1000m)

    @Column(nullable = false, precision = 10, scale = 7)
    private double latitude; //위도

    @Column(nullable = false, precision = 10, scale = 7)
    private Double longitude; //경도

    @Column(nullable = false)
    private Boolean isViewed; // 확인 여부

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criminal_id", nullable = false)
    private Criminal criminal;
}
