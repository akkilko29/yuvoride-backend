package com.yuvoride.backend.mapper;

import com.yuvoride.backend.dto.driver.DriverResponse;
import com.yuvoride.backend.entity.Driver;
import org.springframework.stereotype.Component;

@Component
public class DriverMapper {

    public DriverResponse toResponse(Driver driver) {
        return DriverResponse.builder()
                .id(driver.getId())
                .userId(driver.getUser().getId())
                .licenseNumber(driver.getLicenseNumber())
                .status(driver.getStatus())
                .online(driver.isOnline())
                .available(driver.isAvailable())
                .currentLatitude(driver.getCurrentLatitude())
                .currentLongitude(driver.getCurrentLongitude())
                .rating(driver.getRating())
                .totalRides(driver.getTotalRides())
                .vehicleTypeId(driver.getVehicle() != null ? driver.getVehicle().getVehicleType().getId() : null)
                .vehiclePlate(driver.getVehicle() != null ? driver.getVehicle().getPlateNumber() : null)
                .build();
    }
}
