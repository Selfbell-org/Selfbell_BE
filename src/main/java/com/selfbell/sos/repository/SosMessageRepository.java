package com.selfbell.sos.repository;

import com.selfbell.sos.domain.SosMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SosMessageRepository extends JpaRepository<SosMessage, Long> {
}
