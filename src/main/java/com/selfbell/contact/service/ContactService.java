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

        // 자기 자신 금지
        if (me.getPhoneNumber().equals(to.getPhoneNumber())) {
            throw new ApiException(ErrorCode.SELF_REQUEST_NOT_ALLOWED);
        }

        // 이미 양방향 중 하나라도 존재하면 금지
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

        var meDto = ContactPartyDTO.of(me.getPhoneNumber(), me.getName());
        var otherDto = ContactPartyDTO.of(to.getPhoneNumber(), to.getName());
        return ContactCreateResponseDTO.from(saved, meDto, otherDto);
    }

    @Transactional
    public ContactAcceptResponseDTO accept(Long meId, Long contactId) {
        var me = getUserOr404(meId);
        var contact = contactRepository.findByIdIfParticipant(contactId, me)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_PARTICIPANT));

        if (contact.getStatus() != Status.PENDING) {
            throw new ApiException(ErrorCode.INVALID_STATUS_TRANSITION);
        }

        // 상대방이 보낸 요청인지 확인 (수락은 요청의 상대만 가능)
        boolean iAmReceiver = contact.getContact().getId().equals(me.getId());
        if (!iAmReceiver) {
            throw new ApiException(ErrorCode.NOT_PARTICIPANT, "수락은 요청받은 당사자만 가능합니다.");
        }

        contact.accept();
        // 더 바꿀 값 없으면 그대로 리턴
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
    public ContactListResponseDTO list(Long meId, String statusStr, Pageable pageable) {
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

        Page<Contact> page = contactRepository.findAllForMeWithStatus(me, status, pageable);

        var items = page.getContent().stream()
                .map(c -> ContactListItemDTO.of(c, c.getUser().getId().equals(me.getId())))
                .toList();

        return ContactListResponseDTO.of(items, page);
    }
}
