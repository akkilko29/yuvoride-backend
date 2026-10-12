package com.yuvoride.backend.dto.driver;

import com.yuvoride.backend.enums.DriverAccountStatus;
import com.yuvoride.backend.enums.DriverStatus;
import com.yuvoride.backend.enums.KycStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DriverResponse {
    private final Long id;
    private final Long userId;
    private final String name;
    private final String email;
    private final String phone;
    private final String licenseNumber;
    private final DriverStatus status;
    private final DriverAccountStatus accountStatus;
    private final KycStatus kycStatus;
    private final boolean online;
    private final boolean available;
    private final boolean profileComplete;
    private final Double currentLatitude;
    private final Double currentLongitude;
    private final BigDecimal rating;
    private final Integer totalRides;
    private final Long vehicleTypeId;
    private final String vehicleType;
    private final String vehiclePlate;
    private final String address;
}
