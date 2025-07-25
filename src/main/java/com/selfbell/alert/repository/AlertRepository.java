package com.selfbell.alert.repository;

import com.selfbell.alert.domain.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByUserId(Long userId);  // 사용자별 알림 조회용
}
