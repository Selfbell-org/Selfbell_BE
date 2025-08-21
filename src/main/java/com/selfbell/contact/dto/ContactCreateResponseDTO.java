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
                ContactPartyDTO.from(c.getUser()),
                ContactPartyDTO.from(c.getContact()),
                c.getStatus().name(),
                c.isSharePermission()
        );
    }
}
