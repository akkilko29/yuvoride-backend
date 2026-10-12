package com.yuvoride.backend.dto.driver;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateKycRequest {
    private String name;
    private String dob;
    private String phone;
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
