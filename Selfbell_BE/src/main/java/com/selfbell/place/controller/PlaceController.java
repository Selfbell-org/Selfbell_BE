package com.selfbell.place.controller;

import com.selfbell.place.domain.Place;
import com.selfbell.place.dto.PlaceDto;
import com.selfbell.place.service.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/places")
public class PlaceController {

    private final PlaceService placeService;

    @PostMapping
    public ResponseEntity<Place> savePlace(@RequestBody PlaceDto placeDto) {
        return ResponseEntity.ok(placeService.savePlace(placeDto));
    }

    @GetMapping
    public ResponseEntity<List<Place>> getAllPlaces() {
        return ResponseEntity.ok(placeService.findAll());
    }
}
