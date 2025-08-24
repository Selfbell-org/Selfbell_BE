package com.selfbell.contact.dto;

import com.selfbell.user.domain.User;

public record ContactPartyDTO(
        Long userId,
        String phoneNumber,
        String name
) {
    public static ContactPartyDTO of(User u) {
        return new ContactPartyDTO(
                u.getId(),
                u.getPhoneNumber(),
                u.getName()
        );
    }
}

