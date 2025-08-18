package com.selfbell.contact.dto;

public record ContactPartyDTO(
        String phoneNumber,
        String name
) {
    public static ContactPartyDTO of(String phoneNumber, String name) {
        return new ContactPartyDTO(phoneNumber, name);
    }
}
