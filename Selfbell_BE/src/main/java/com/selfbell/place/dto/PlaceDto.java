package com.selfbell.place.dto;

import com.selfbell.place.domain.Place;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceDto {

    private String placeName;  // 장소 이름
    private String address;    // 주소
    private Double latitude;   // 위도
    private Double longitude;  // 경도
    private int radiusM;       // 반경 (100~1000)
    private Boolean isOnAlarm; // 알림 여부

    // Place → PlaceDto 변환
    public static PlaceDto fromEntity(Place place) {
        return PlaceDto.builder()
                .placeName(place.getPlaceName())
                .address(place.getAddress())
                .latitude(place.getLatitude())
                .longitude(place.getLongitude())
                .radiusM(place.getRadiusM())
                .isOnAlarm(place.getIsOnAlarm())
                .build();
    }

    // PlaceDto → Place 변환
    public Place toEntity() {
        return Place.builder()
                .placeName(this.placeName)
                .address(this.address)
                .latitude(this.latitude)
                .longitude(this.longitude)
                .radiusM(this.radiusM)
                .isOnAlarm(this.isOnAlarm)
                .build();
    }
}
