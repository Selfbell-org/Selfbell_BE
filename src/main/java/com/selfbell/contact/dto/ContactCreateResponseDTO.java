package com.selfbell.contact.dto;

import com.selfbell.contact.domain.Contact;
import com.selfbell.contact.domain.enums.Status;

public record ContactCreateResponseDTO(
        Long contactId,
        ContactPartyDTO me,
        ContactPartyDTO other,
        Status status,
        boolean sharePermission
) {
    public static ContactCreateResponseDTO from(Contact c, ContactPartyDTO me, ContactPartyDTO other) {
        return new ContactCreateResponseDTO(
                c.getId(), me, other, c.getStatus(), c.isSharePermission()
        );
    }
}
