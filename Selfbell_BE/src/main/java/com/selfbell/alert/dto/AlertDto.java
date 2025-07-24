package com.selfbell.alert.dto;

import com.selfbell.alert.domain.Alert;
import com.selfbell.place.domain.Place;
import com.selfbell.user.domain.User;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertDto {

    private Long id;
    private String message;
    private LocalDateTime sentAt;
    private int radiusM;
    private double latitude;
    private double longitude;
    private Boolean isViewed;
    private Long placeId;
    private Long userId;

    // Alert 엔티티 → DTO 변환 메서드
    public static AlertDto from(Alert alert) {
        return AlertDto.builder()
                .id(alert.getId())
                .message(alert.getMessage())
                .sentAt(alert.getSentAt())
                .radiusM(alert.getRadiusM())
                .latitude(alert.getLatitude())
                .longitude(alert.getLongitude())
                .isViewed(alert.getIsViewed())
                .placeId(alert.getPlace().getId())
                .userId(alert.getUser().getId())
                .build();
    }

    // DTO → Alert 엔티티 변환 메서드
    public Alert toEntity(Place place, User user) {
        return Alert.builder()
                .message(this.message)
                .sentAt(this.sentAt != null ? this.sentAt : LocalDateTime.now())
                .latitude(this.latitude)
                .longitude(this.longitude)
                .radiusM(this.radiusM)
                .isViewed(this.isViewed)
                .place(place)
                .user(user)
                .build();
    }
}
