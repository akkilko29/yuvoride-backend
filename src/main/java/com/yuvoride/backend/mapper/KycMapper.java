package com.yuvoride.backend.mapper;

import com.yuvoride.backend.dto.driver.KycResponse;
import com.yuvoride.backend.entity.Driver;
import com.yuvoride.backend.entity.DriverKyc;
import com.yuvoride.backend.enums.KycStatus;
import org.springframework.stereotype.Component;

@Component
public class KycMapper {

    public KycResponse toResponse(Driver driver, DriverKyc kyc) {
        if (kyc == null) {
            return KycResponse.builder()
                    .driverId(driver.getId())
                    .status(driver.getKycStatus() != null ? driver.getKycStatus() : KycStatus.PENDING)
                    .dob(driver.getDob())
                    .address(driver.getAddress())
                    .identityType(driver.getIdentityType())
                    .identityNumber(driver.getIdentityNumber())
                    .photoRef(driver.getPhotoRef())
                    .identityRef(driver.getIdentityRef())
                    .rcRef(driver.getRcRef())
                    .licenseRef(driver.getLicenseRef())
                    .licenseNumber(driver.getLicenseNumber())
                    .vehicleNumber(driver.getVehicleNumber())
                    .rcNumber(driver.getRcNumber())
                    .build();
        }
        return KycResponse.builder()
                .driverId(driver.getId())
                .status(kyc.getStatus())
                .rejectionReason(kyc.getRejectionReason())
                .dob(kyc.getDob())
                .address(kyc.getAddress())
                .identityType(kyc.getIdentityType())
                .identityNumber(kyc.getIdentityNumber())
                .photoRef(kyc.getPhotoRef())
                .identityRef(kyc.getIdentityRef())
                .rcRef(kyc.getRcRef())
                .licenseRef(kyc.getLicenseRef())
                .licenseNumber(kyc.getLicenseNumber())
                .vehicleNumber(kyc.getVehicleNumber())
                .rcNumber(kyc.getRcNumber())
                .vehicleType(kyc.getVehicleType())
                .submittedAt(kyc.getSubmittedAt())
                .reviewedAt(kyc.getReviewedAt())
                .build();
    }
}
