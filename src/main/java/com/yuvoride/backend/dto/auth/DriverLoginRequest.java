package com.yuvoride.backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DriverLoginRequest {
    @NotBlank
    @Email
    private String email;
    private String role;
}
