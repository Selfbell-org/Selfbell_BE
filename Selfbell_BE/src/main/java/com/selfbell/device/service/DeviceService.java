package com.selfbell.device.service;

import com.selfbell.device.domain.Device;
import com.selfbell.device.dto.DeviceRequestDto;
import com.selfbell.device.dto.DeviceResponseDto;
import com.selfbell.device.repository.DeviceRepository;
import com.selfbell.user.domain.User;
import com.selfbell.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;

    public DeviceResponseDto registerDevice(DeviceRequestDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 1:1 관계 → 이미 등록된 기기 존재하면 예외
        deviceRepository.findByUser(user).ifPresent(d -> {
            throw new IllegalStateException("이미 등록된 디바이스가 존재합니다.");
        });

        Device device = Device.builder()
                .user(user)
                .deviceToken(dto.getDeviceToken())
                .deviceType(dto.getDeviceType())
                .createdAt(LocalDateTime.now())
                .build();

        Device saved = deviceRepository.save(device);

        return DeviceResponseDto.builder()
                .id(saved.getId())
                .userId(user.getId())
                .deviceToken(saved.getDeviceToken())
                .deviceType(saved.getDeviceType())
                .build();
    }
}
