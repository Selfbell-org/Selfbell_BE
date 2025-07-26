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

    @Builder.Default
    @Min(100)
    @Max(1000)
    @Column(nullable = false, columnDefinition = "int default 500")
    private int radiusM = 500;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "TINYINT(1) default 0")
    private Boolean isViewed = false;

    @Column(nullable = false)
    private LocalDateTime triggeredAt;

    @Column(nullable = false)
    private LocalDateTime sendAt;

    @Column(nullable = false, precision = 10, scale = 7)
    private double latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private Double longitude;

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
