package com.yuvoride.backend.service.impl;

import com.yuvoride.backend.config.AppProperties;
import com.yuvoride.backend.entity.Driver;
import com.yuvoride.backend.entity.Ride;
import com.yuvoride.backend.repository.DriverRepository;
import com.yuvoride.backend.service.DriverMatchingService;
import com.yuvoride.backend.service.NotificationService;
import com.yuvoride.backend.util.GeoUtils;
import com.yuvoride.backend.websocket.RealtimePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverMatchingServiceImpl implements DriverMatchingService {

    private final DriverRepository driverRepository;
    private final AppProperties properties;
    private final RealtimePublisher realtimePublisher;
    private final NotificationService notificationService;

    @Override
    public List<Driver> findEligibleDrivers(Ride ride) {
        double radius = properties.getDriverMatch().getRadiusKm();
        int maxAge = properties.getDriverMatch().getLocationMaxAgeSeconds();
        Instant minLocationAt = Instant.now().minusSeconds(maxAge);
        return driverRepository.findEligibleForVehicleType(ride.getVehicleType().getId()).stream()
                .filter(driver -> driver.getLastLocationAt() == null || !driver.getLastLocationAt().isBefore(minLocationAt))
                .filter(driver -> GeoUtils.haversineKm(
                        ride.getPickupLatitude(), ride.getPickupLongitude(),
                        driver.getCurrentLatitude(), driver.getCurrentLongitude()) <= radius)
                .sorted(Comparator.comparingDouble(driver -> GeoUtils.haversineKm(
                        ride.getPickupLatitude(), ride.getPickupLongitude(),
                        driver.getCurrentLatitude(), driver.getCurrentLongitude())))
                .toList();
    }

    @Override
    public void offerRide(Ride ride) {
        List<Driver> drivers = findEligibleDrivers(ride);
        realtimePublisher.rideOfferedToDrivers(ride, drivers);
        drivers.forEach(driver -> notificationService.notifyDriverRideRequest(driver, ride));
        log.info("Offered ride {} to {} eligible drivers", ride.getId(), drivers.size());
    }
}
