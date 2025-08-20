package com.selfbell.contact.dto;

import com.selfbell.contact.domain.Contact;

public record ContactCreateResponseDTO(
        Long contactId,
        ContactPartyDTO me,
        ContactPartyDTO other,
        String status,
        boolean sharePermission
) {
    public static ContactCreateResponseDTO from(Contact c) {
        return new ContactCreateResponseDTO(
                c.getId(),
                ContactPartyDTO.of(c.getUser()),
                ContactPartyDTO.of(c.getContact()),
                c.getStatus().name(),
                c.isSharePermission()
        );
    }
}
