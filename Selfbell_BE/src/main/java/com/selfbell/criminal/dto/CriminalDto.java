package com.selfbell.criminal.dto;

import com.selfbell.criminal.domain.Criminal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CriminalDto {

    private final Long id;
    private final String name;
    private final int age;
    private final String address;
    private final String crimeType;

    public static CriminalDto from(Criminal criminal) {
        return CriminalDto.builder()
                .id(criminal.getId())
                .name(criminal.getName())
                .age(criminal.getAge())
                .address(criminal.getAddress())
                .crimeType(criminal.getCrimeType())
                .build();
    }

    public Criminal toEntity() {
        return Criminal.builder()
                .name(name)
                .age(age)
                .address(address)
                .crimeType(crimeType)
                .build();
    }
}
