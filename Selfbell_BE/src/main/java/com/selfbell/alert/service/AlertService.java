package com.selfbell.alert.service;

import com.selfbell.alert.domain.Alert;
import com.selfbell.alert.dto.AlertDto;
import com.selfbell.alert.repository.AlertRepository;
import com.selfbell.place.domain.Place;
import com.selfbell.place.repository.PlaceRepository;
import com.selfbell.user.domain.User;
import com.selfbell.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;

    public AlertDto createAlert(AlertDto alertDto) {
        Place place = placeRepository.findById(alertDto.placeId())
                .orElseThrow(() -> new IllegalArgumentException("Place not found"));

        User user = userRepository.findById(alertDto.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Alert alert = alertDto.toEntity(place, user);
        return AlertDto.from(alertRepository.save(alert));
    }

    public List<AlertDto> getAlertsByUser(Long userId) {
        return alertRepository.findByUserId(userId).stream()
                .map(AlertDto::from)
                .toList();
    }
}

