package com.selfbell.contact.service;

import com.selfbell.contact.domain.Contact;
import com.selfbell.contact.domain.enums.Status;
import com.selfbell.contact.dto.*;
import com.selfbell.contact.repository.ContactRepository;
import com.selfbell.global.error.ApiException;
import com.selfbell.global.error.ErrorCode;
import com.selfbell.user.domain.User;
import com.selfbell.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final UserRepository userRepository;
    private final ContactRepository contactRepository;

    private User getUserOr404ByPhone(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
    }

    private User getUserOr404(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    @Transactional
    public ContactCreateResponseDTO createRequest(Long meId, ContactRequestCreateDTO req) {
        var me = getUserOr404(meId);
        var to = getUserOr404ByPhone(req.toPhoneNumber());

        if (me.getPhoneNumber().equals(to.getPhoneNumber())) {
            throw new ApiException(ErrorCode.SELF_REQUEST_NOT_ALLOWED);
        }

        boolean exists = contactRepository.existsByUserAndContactOrUserAndContact(me, to, to, me);
        if (exists) {
            throw new ApiException(ErrorCode.CONTACT_ALREADY_EXISTS);
        }

        var contact = Contact.builder()
                .user(me)
                .contact(to)
                .relation("FRIEND")
                .status(Status.PENDING)
                .sharePermission(false)
                .build();

        var saved = contactRepository.save(contact);

        return ContactCreateResponseDTO.from(saved);
    }

    @Transactional
    public ContactAcceptResponseDTO accept(Long meId, Long contactId) {
        var me = getUserOr404(meId);
        var contact = contactRepository.findByIdIfParticipant(contactId, me)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_PARTICIPANT));

        if (contact.getStatus() != Status.PENDING) {
            throw new ApiException(ErrorCode.INVALID_STATUS_TRANSITION);
        }

        boolean iAmReceiver = contact.getContact().getId().equals(me.getId());
        if (!iAmReceiver) {
            throw new ApiException(ErrorCode.NOT_PARTICIPANT, "수락은 요청받은 당사자만 가능합니다.");
        }

        contact.accept();

        return ContactAcceptResponseDTO.from(contact);
    }

    @Transactional
    public ContactAcceptResponseDTO updateSharePermission(Long contactId, SharePermissionRequest request) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ApiException(ErrorCode.CONTACT_NOT_FOUND));

        if (contact.getStatus() != Status.ACCEPTED) {
            throw new ApiException(ErrorCode.SHARE_CHANGE_NOT_ALLOWED);
        }

        contact.updateSharePermission(request.allow());
        return ContactAcceptResponseDTO.from(contact);
    }

    @Transactional(readOnly = true)
    public ContactListResponseDTO list(Long meId, String statusStr, String box, Pageable pageable) {
        var me = getUserOr404(meId);

        Status status = null;
        if (statusStr != null) {
            try {
                status = Status.valueOf(statusStr.toUpperCase());
                if (status != Status.PENDING && status != Status.ACCEPTED) {
                    throw new IllegalArgumentException();
                }
            } catch (Exception e) {
                throw new ApiException(ErrorCode.INVALID_STATUS_FILTER);
            }
        }

        String b = (box == null || box.isBlank()) ? "ALL" : box.trim().toUpperCase();

        Page<Contact> page;
        switch (b) {
            case "SENT" -> {
                if (status != null) page = contactRepository.findByUserAndStatus(me, status, pageable);
                else page = contactRepository.findByUserOrContactAndStatus(me, me, Status.PENDING, pageable); // 필요 시 보완
            }
            case "RECEIVED" -> {
                if (status != null) page = contactRepository.findByContactAndStatus(me, status, pageable);
                else {
                    page = contactRepository.findByContactAndStatus(me, Status.PENDING, pageable); // 임시: 주로 요청함 페이지용
                }
            }
            default -> { // ALL
                if (status != null) page = contactRepository.findByUserOrContactAndStatus(me, me, status, pageable);
                else page = contactRepository.findByUserOrContact(me, me, pageable);
            }
        }

        var items = page.getContent().stream()
                .map(c -> ContactListItemDTO.of(c, c.getUser().getId().equals(me.getId())))
                .toList();

        return ContactListResponseDTO.of(items, page);
    }
}
