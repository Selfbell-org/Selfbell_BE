package com.selfbell.criminal.repository;

import com.selfbell.criminal.domain.Criminal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CriminalRepository extends JpaRepository<Criminal, Long> {
}
