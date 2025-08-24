package com.selfbell.contact.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record ContactListResponseDTO(
        List<ContactListItemDTO> items,
        PageMeta page
) {
    public record PageMeta(int size, int number, long totalElements, int totalPages) {}

    public static ContactListResponseDTO of(List<ContactListItemDTO> items, Page<?> page) {
        return new ContactListResponseDTO(
                items,
                new PageMeta(page.getSize(), page.getNumber(), page.getTotalElements(), page.getTotalPages())
        );
    }
}
