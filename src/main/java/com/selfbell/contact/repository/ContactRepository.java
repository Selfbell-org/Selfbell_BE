package com.selfbell.contact.repository;

import com.selfbell.contact.domain.Contact;
import com.selfbell.contact.domain.enums.Status;
import com.selfbell.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    boolean existsByUserAndContact(User user, User contact);
    boolean existsByUserAndContactOrUserAndContact(User u1, User c1, User u2, User c2);

    @Query("""
           select c from Contact c
           where (c.user = :me or c.contact = :me)
           and (:status is null or c.status = :status)
           """)
    Page<Contact> findAllForMeWithStatus(@Param("me") User me,
                                         @Param("status") Status status,
                                         Pageable pageable);

    @Query("""
           select c from Contact c
           where c.id = :id and (c.user = :me or c.contact = :me)
           """)
    Optional<Contact> findByIdIfParticipant(@Param("id") Long id, @Param("me") User me);

    Page<Contact> findByUserAndStatus(User user, Status status, Pageable pageable);     // 보낸 요청/친구
    Page<Contact> findByContactAndStatus(User contact, Status status, Pageable pageable); // 받은 요청/친구
    Page<Contact> findByUserOrContact(User u1, User u2, Pageable pageable);              // 전체
    Page<Contact> findByUserOrContactAndStatus(User u1, User u2, Status status, Pageable pageable);

}
