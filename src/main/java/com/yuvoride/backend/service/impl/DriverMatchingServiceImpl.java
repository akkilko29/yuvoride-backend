package com.yuvoride.backend.service.impl;

import com.yuvoride.backend.config.AppProperties;
import com.yuvoride.backend.entity.Driver;
import com.yuvoride.backend.entity.Ride;
import com.yuvoride.backend.enums.DriverStatus;
import com.yuvoride.backend.enums.RideStatus;
import com.yuvoride.backend.repository.DriverRepository;
import com.yuvoride.backend.service.DriverMatchingService;
import com.yuvoride.backend.service.RideStateMachine;
import com.yuvoride.backend.util.GeoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverMatchingServiceImpl implements DriverMatchingService {

    private final DriverRepository driverRepository;
    private final RideStateMachine rideStateMachine;
    private final AppProperties properties;

    @Override
    @Transactional
    public void assignDriver(Ride ride) {
        rideStateMachine.assertTransition(ride.getStatus(), RideStatus.SEARCHING_DRIVER);
        ride.setStatus(RideStatus.SEARCHING_DRIVER);

        List<Driver> candidates = driverRepository.findAvailableForVehicleType(ride.getVehicleType().getId());
        double radius = properties.getDriverMatch().getRadiusKm();
        Driver selected = candidates.stream()
                .filter(driver -> GeoUtils.haversineKm(
                        ride.getPickupLatitude(), ride.getPickupLongitude(),
                        driver.getCurrentLatitude(), driver.getCurrentLongitude()) <= radius)
                .min(Comparator.comparingDouble(driver -> GeoUtils.haversineKm(
                        ride.getPickupLatitude(), ride.getPickupLongitude(),
                        driver.getCurrentLatitude(), driver.getCurrentLongitude())))
                .orElse(null);

        if (selected == null) {
            rideStateMachine.assertTransition(ride.getStatus(), RideStatus.NO_DRIVER_FOUND);
            ride.setStatus(RideStatus.NO_DRIVER_FOUND);
            log.info("No driver found for ride {}", ride.getId());
            return;
        }

        rideStateMachine.assertTransition(ride.getStatus(), RideStatus.DRIVER_ASSIGNED);
        selected.setAvailable(false);
        selected.setStatus(DriverStatus.BUSY);
        ride.setDriver(selected);
        ride.setStatus(RideStatus.DRIVER_ASSIGNED);
        driverRepository.save(selected);
        log.info("Assigned driver {} to ride {}", selected.getId(), ride.getId());
    }
}
