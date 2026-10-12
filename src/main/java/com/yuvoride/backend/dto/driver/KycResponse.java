package com.yuvoride.backend.dto.driver;

import com.yuvoride.backend.enums.IdentityType;
import com.yuvoride.backend.enums.KycStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Builder
public class KycResponse {
    private final Long driverId;
    private final KycStatus status;
    private final String rejectionReason;
    private final LocalDate dob;
    private final String address;
    private final IdentityType identityType;
    private final String identityNumber;
    private final String photoRef;
    private final String identityRef;
    private final String rcRef;
    private final String licenseRef;
    private final String licenseNumber;
    private final String vehicleNumber;
    private final String rcNumber;
    private final String vehicleType;
    private final Instant submittedAt;
    private final Instant reviewedAt;
}
