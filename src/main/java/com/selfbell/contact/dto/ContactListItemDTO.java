package com.selfbell.contact.dto;

import com.selfbell.contact.domain.Contact;
import com.selfbell.contact.domain.enums.Status;

public record ContactListItemDTO(
        Long contactId,
        ContactPartyDTO other,
        Status status,
        boolean sharePermission,
        Contact.Direction direction
) {
    public static ContactListItemDTO of(Contact contact, boolean meIsUserSide) {
        var otherUser = meIsUserSide ? contact.getContact() : contact.getUser();
        var dir = meIsUserSide ? Contact.Direction.SENT : Contact.Direction.RECEIVED;
        return new ContactListItemDTO(
                contact.getId(),
                ContactPartyDTO.of(otherUser),
                contact.getStatus(),
                contact.isSharePermission(),
                dir
        );
    }
}
