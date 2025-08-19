package com.selfbell.safewalk.domain;

import com.selfbell.safewalk.domain.enums.SafeWalkStatus;
import com.selfbell.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "safe_walk_session")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SafeWalkSession {  // BaseTimeEntity 상속 안함!

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ward_id", nullable = false)
    private User ward;

    // 출발지
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "lat", column = @Column(name = "origin_lat", precision = 10, scale = 7, nullable = false)),
        @AttributeOverride(name = "lon", column = @Column(name = "origin_lon", precision = 10, scale = 7, nullable = false))
    })
    private GeoPoint origin;

    @Column(name = "origin_address")
    private String originAddress;

    // 도착지
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "lat", column = @Column(name = "destination_lat", precision = 10, scale = 7, nullable = false)),
        @AttributeOverride(name = "lon", column = @Column(name = "destination_lon", precision = 10, scale = 7, nullable = false))
    })
    private GeoPoint destination;

    @Column(name = "destination_address")
    private String destinationAddress;

    // 도착시각 or 타이머 종료시각(둘 중 하나 사용)
    private LocalDateTime expectedArrival;
    private LocalDateTime timerEnd;

    @CreatedDate
    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private SafeWalkStatus safeWalkStatus;

//    @Version
//    @Column(nullable = false)
//    private Long version = 0L;

    public static SafeWalkSession createSession(
            User user, GeoPoint origin, String originAddress,
            GeoPoint destination, String destinationAddress,
            LocalDateTime expectedArrival, LocalDateTime timerEnd,
            LocalDateTime endedAt, SafeWalkStatus status
    ) {
        return SafeWalkSession.builder()
                .ward(user)
                .origin(origin)
                .originAddress(originAddress)
                .destination(destination)
                .destinationAddress(destinationAddress)
                .expectedArrival(expectedArrival)
                .timerEnd(timerEnd)
                .endedAt(endedAt)
                .safeWalkStatus(status)
                .build();
    }

    public boolean isActive() {
        return safeWalkStatus == SafeWalkStatus.IN_PROGRESS && endedAt == null;
    }

    public void endSession(){
        this.endedAt = LocalDateTime.now();
        this.safeWalkStatus = SafeWalkStatus.MANUAL_END;
    }
}
