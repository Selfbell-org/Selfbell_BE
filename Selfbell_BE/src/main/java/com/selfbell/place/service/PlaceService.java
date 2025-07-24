package com.selfbell.place.service;

import com.selfbell.place.domain.Place;
import com.selfbell.place.dto.PlaceDto;
import com.selfbell.place.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceService {

    private final PlaceRepository placeRepository;

    public Place savePlace(PlaceDto placeDto) {
        Place place = Place.builder()
                .placeName(placeDto.getPlaceName())
                .latitude(placeDto.getLatitude())
                .longitude(placeDto.getLongitude())
                .build();

        return placeRepository.save(place);
    }

    public List<Place> findAll() {
        return placeRepository.findAll();
    }
}

