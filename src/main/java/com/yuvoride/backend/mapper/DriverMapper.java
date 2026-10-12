package com.yuvoride.backend.mapper;

import com.yuvoride.backend.dto.driver.DriverResponse;
import com.yuvoride.backend.entity.Driver;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class DriverMapper {

    public DriverResponse toResponse(Driver driver) {
        boolean complete = StringUtils.hasText(driver.getLicenseNumber()) && StringUtils.hasText(driver.getVehicleNumber());
        return DriverResponse.builder()
                .id(driver.getId())
                .userId(driver.getUser().getId())
                .name(driver.getUser().getName())
                .email(driver.getUser().getEmail())
                .phone(driver.getUser().getPhone())
                .licenseNumber(driver.getLicenseNumber())
                .status(driver.getStatus())
                .accountStatus(driver.getAccountStatus())
                .kycStatus(driver.getKycStatus())
                .online(driver.isOnline())
                .available(driver.isAvailable())
                .profileComplete(complete)
                .currentLatitude(driver.getCurrentLatitude())
                .currentLongitude(driver.getCurrentLongitude())
                .rating(driver.getRating())
                .totalRides(driver.getTotalRides())
                .vehicleTypeId(driver.getPreferredVehicleType() != null
                        ? driver.getPreferredVehicleType().getId()
                        : driver.getVehicle() != null ? driver.getVehicle().getVehicleType().getId() : null)
                .vehicleType(driver.getPreferredVehicleType() != null
                        ? driver.getPreferredVehicleType().getName()
                        : driver.getVehicle() != null ? driver.getVehicle().getVehicleType().getName() : null)
                .vehiclePlate(driver.getVehicleNumber() != null
                        ? driver.getVehicleNumber()
                        : driver.getVehicle() != null ? driver.getVehicle().getPlateNumber() : null)
                .address(driver.getAddress())
                .build();
    }
}
