package com.yuvoride.backend.dto.auth;

import com.yuvoride.backend.util.AuthIdentity;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

@Getter
@Setter
public class VerifyOtpRequest {

    @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "Phone must be in E.164 format, e.g. +919999999999")
    private String phone;

    @Email
    private String email;

    @NotBlank(message = "OTP is required")
    @Pattern(regexp = "^\\d{6}$", message = "OTP must be 6 digits")
    private String otp;

    public String destination() {
        return AuthIdentity.destination(phone, email);
    }

    @AssertTrue(message = "Provide either phone or email")
    public boolean isDestinationPresent() {
        return StringUtils.hasText(phone) ^ StringUtils.hasText(email);
    }
}
