package com.yuvoride.backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DriverRegisterRequest {
    @NotBlank
    @Size(min = 2, max = 100)
    private String name;
    private String dob;
    @NotBlank
    private String phone;
    @NotBlank
    @Email
    private String email;
    private String address;
    private String photoUri;
    private String identityType;
    private String identityNumber;
    private String identityUri;
    private String vehicleType;
    private String vehicleNumber;
    private String rcNumber;
    private String rcUri;
    private String licenseNumber;
    private String licenseUri;
}
