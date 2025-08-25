package com.selfbell.sos.repository;

import com.selfbell.sos.domain.SosMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SosRecipientRepository extends JpaRepository<SosMessage, Long> {
}
