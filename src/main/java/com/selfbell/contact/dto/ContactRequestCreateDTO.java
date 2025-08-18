package com.selfbell.contact.dto;

import jakarta.validation.constraints.NotBlank;

public record ContactRequestCreateDTO(
        @NotBlank(message = "toPhoneNumber는 필수입니다.")
        String toPhoneNumber
) {}
