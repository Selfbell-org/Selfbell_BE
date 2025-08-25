package com.selfbell.sos.repository;

import com.selfbell.sos.domain.SosRecipient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SosRecipientRepository extends JpaRepository<SosRecipient, Long> {
}
