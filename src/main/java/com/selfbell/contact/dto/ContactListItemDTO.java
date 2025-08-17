package com.selfbell.contact.dto;

import com.selfbell.contact.domain.Contact;
import com.selfbell.contact.domain.enums.Status;

public record ContactListItemDTO(
        Long contactId,
        ContactPartyDTO other,
        Status status,
        boolean sharePermission
) {
    public static ContactListItemDTO of(Contact contact, boolean meIsUserSide) {
        var otherUser = meIsUserSide ? contact.getContact() : contact.getUser();
        return new ContactListItemDTO(
                contact.getId(),
                ContactPartyDTO.of(otherUser.getPhoneNumber(), otherUser.getName()),
                contact.getStatus(),
                contact.isSharePermission()
        );
    }
}
