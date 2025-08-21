package com.selfbell.contact.dto;

import com.selfbell.user.domain.User;

public record ContactPartyDTO(
        Long userId,
        String phoneNumber,
        String name
) {
    public static ContactPartyDTO of(Long userId, String phoneNumber, String name) {
        return new ContactPartyDTO(userId, phoneNumber, name);
    }

    public static ContactPartyDTO from(User user) {
        return new ContactPartyDTO(
                user.getId(),
                user.getPhoneNumber(),
                user.getName()
        );
    }
}

