package com.yuvoride.backend.dto.driver;

import com.yuvoride.backend.enums.DriverStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DriverResponse {
    private final Long id;
    private final Long userId;
    private final String licenseNumber;
    private final DriverStatus status;
    private final boolean online;
    private final boolean available;
    private final Double currentLatitude;
    private final Double currentLongitude;
    private final BigDecimal rating;
    private final Integer totalRides;
    private final Long vehicleTypeId;
    private final String vehiclePlate;
}
