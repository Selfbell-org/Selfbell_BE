package com.selfbell.user.service;

import com.selfbell.device.domain.Device;
import com.selfbell.device.domain.enums.DeviceType;
import com.selfbell.device.repository.DeviceRepository;
import com.selfbell.global.error.ApiException;
import com.selfbell.global.error.ErrorCode;
import com.selfbell.user.domain.User;
import com.selfbell.user.dto.UserPhoneResponse;
import com.selfbell.user.dto.UserSignUpRequestDTO;
import com.selfbell.user.exception.UserNotFoundException;
import com.selfbell.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User createUser(UserSignUpRequestDTO request) {
        validateDuplicatePhoneNumber(request.getPhoneNumber());

        // 1) 유저 저장
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User user = request.toUser(hashedPassword);
        userRepository.save(user);

        // 2) 디바이스 정보 처리 (둘 다 있으면 저장, 하나만 있으면 400)
        boolean hasToken = request.getDeviceToken() != null && !request.getDeviceToken().isBlank();
        DeviceType type = request.toDeviceTypeOrNull(); // null 허용 + 값 검증은 DTO에서

        boolean hasType = (type != null);
        if (hasToken ^ hasType) {
            // 하나만 온 경우
            throw new ApiException(
                    ErrorCode.INVALID_INPUT,
                    "deviceToken과 deviceType은 함께 제공되어야 합니다."
            );
        }

        if (hasToken) {
            Device device = Device.builder()
                    .user(user)
                    .deviceToken(request.getDeviceToken())
                    .deviceType(type)
                    .build();
            deviceRepository.save(device);
        }

        return user;
    }

    @Transactional(readOnly = true)
    public UserPhoneResponse checkUserByPhoneNumber(String phoneNumber) {
        boolean hasUser = userRepository.existsByPhoneNumber(phoneNumber);
        return new UserPhoneResponse(hasUser);
    }

    private void validateDuplicatePhoneNumber(String phoneNumber) {
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new ApiException(ErrorCode.INVALID_INPUT, "이미 등록된 전화번호입니다.");
        }
    }

    public User findByIdOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }
}
