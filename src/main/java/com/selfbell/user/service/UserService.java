package com.selfbell.user.service;

import com.selfbell.device.domain.Device;
import com.selfbell.device.repository.DeviceRepository;
import com.selfbell.user.domain.User;
import com.selfbell.user.dto.UserSignUpRequestDTO;
import com.selfbell.user.exception.UserNotFoundException;
import com.selfbell.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User createUser(UserSignUpRequestDTO request) {
        validateDuplicatePhoneNumber(request.getPhoneNumber());

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User user = request.toUser(hashedPassword);
        userRepository.save(user);

        Device device = Device.builder()
                .user(user)
                .deviceToken(request.getDeviceToken())
                .deviceType(request.toDeviceType())
                .build();
        deviceRepository.save(device);

        return user;
    }

    private void validateDuplicatePhoneNumber(String phoneNumber) {
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new IllegalArgumentException("이미 등록된 전화번호입니다.");
        }
    }

    public User findByIdOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }
}

