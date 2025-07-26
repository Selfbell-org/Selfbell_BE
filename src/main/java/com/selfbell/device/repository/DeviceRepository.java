package com.selfbell.device.repository;

import com.selfbell.device.domain.Device;
import com.selfbell.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByUser(User user);  // 중복 방지용 (1:1 관계 보장)
}
