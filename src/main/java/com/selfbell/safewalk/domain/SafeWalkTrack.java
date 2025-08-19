package com.selfbell.safewalk.domain;

import com.selfbell.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "safe_walk_track")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SafeWalkTrack extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private SafeWalkSession session;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "lat", column = @Column(name = "lat", precision = 10, scale = 7, nullable = false)),
        @AttributeOverride(name = "lon", column = @Column(name = "lon", precision = 10, scale = 7, nullable = false))
    })
    private GeoPoint point;

    @Column(name = "accuracy_m")
    private Double accuracyM;

    @Column(name = "captured_at", nullable = false)
    private LocalDateTime capturedAt; // 단말 시각

    public static SafeWalkTrack createTrack(
            SafeWalkSession session,
            GeoPoint point,
            Double accuracyM,
            LocalDateTime capturedAt) {
        return SafeWalkTrack.builder()
                .session(session)
                .point(point)
                .accuracyM(accuracyM)
                .capturedAt(capturedAt)
                .build();
    }
}
