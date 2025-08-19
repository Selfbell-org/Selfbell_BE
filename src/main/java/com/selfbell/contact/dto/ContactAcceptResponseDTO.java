package com.selfbell.contact.dto;

import com.selfbell.contact.domain.Contact;
import com.selfbell.contact.domain.enums.Status;

public record ContactAcceptResponseDTO(
        Long contactId,
        Status status,
        boolean sharePermission
) {
    public static ContactAcceptResponseDTO from(Contact c) {
        return new ContactAcceptResponseDTO(c.getId(), c.getStatus(), c.isSharePermission());
    }
}
