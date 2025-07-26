package com.selfbell.contact.repository;

import com.selfbell.contact.domain.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    Optional<Contact> findByUserIdAndContactId(Long userId, Long contactId);

    List<Contact> findAllByUserId(Long userId);
}
