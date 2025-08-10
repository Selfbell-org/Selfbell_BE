package com.selfbell.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class LoginRequestDTO {
    @NotBlank
    private String phoneNumber;

    @NotBlank
    private String password;
}
