package com.yuvoride.backend.service.impl;

import com.yuvoride.backend.dto.driver.DriverLocationRequest;
import com.yuvoride.backend.dto.driver.DriverResponse;
import com.yuvoride.backend.dto.driver.PatchDriverRequest;
import com.yuvoride.backend.dto.driver.UpdateDriverAvailabilityRequest;
import com.yuvoride.backend.dto.driver.UpdateDriverLocationRequest;
import com.yuvoride.backend.entity.Driver;
import com.yuvoride.backend.enums.DriverStatus;
import com.yuvoride.backend.enums.RideStatus;
import com.yuvoride.backend.exception.ForbiddenException;
import com.yuvoride.backend.exception.UnprocessableException;
import com.yuvoride.backend.exception.ErrorCodes;
import com.yuvoride.backend.mapper.DriverMapper;
import com.yuvoride.backend.repository.DriverRepository;
import com.yuvoride.backend.repository.RideRepository;
import com.yuvoride.backend.service.DriverService;
import com.yuvoride.backend.websocket.RealtimePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final DriverMapper driverMapper;
    private final RideRepository rideRepository;
    private final RealtimePublisher realtimePublisher;

    @Override
    @Transactional(readOnly = true)
    public DriverResponse me(Long userId) {
        return driverMapper.toResponse(getDriver(userId));
    }

    @Override
    @Transactional
    public DriverResponse updateProfile(Long userId, PatchDriverRequest request) {
        Driver driver = getDriver(userId);
        if (StringUtils.hasText(request.getName())) {
            driver.getUser().setName(request.getName().trim());
        }
        if (StringUtils.hasText(request.getPhone())) {
            driver.getUser().setPhone(request.getPhone().trim());
        }
        if (request.getAddress() != null) {
            driver.setAddress(request.getAddress());
        }
        if (request.getVehicleNumber() != null) {
            driver.setVehicleNumber(request.getVehicleNumber());
        }
        return driverMapper.toResponse(driverRepository.save(driver));
    }

    @Override
    @Transactional
    public DriverResponse updateLocation(Long userId, UpdateDriverLocationRequest request) {
        DriverLocationRequest body = new DriverLocationRequest();
        body.setLatitude(request.getLatitude());
        body.setLongitude(request.getLongitude());
        return updateLocation(userId, body);
    }

    @Override
    @Transactional
    public DriverResponse updateLocation(Long userId, DriverLocationRequest request) {
        Driver driver = getDriver(userId);
        driver.setCurrentLatitude(request.resolvedLat());
        driver.setCurrentLongitude(request.resolvedLng());
        driver.setLastLocationAt(Instant.now());
        driverRepository.save(driver);
        realtimePublisher.driverLocation(driver);
        return driverMapper.toResponse(driver);
    }

    @Override
    @Transactional
    public DriverResponse updateAvailability(Long userId, UpdateDriverAvailabilityRequest request) {
        Driver driver = getDriver(userId);
        boolean goingOnline = Boolean.TRUE.equals(request.getOnline());
        if (goingOnline && !driver.canGoOnline()) {
            throw new UnprocessableException(ErrorCodes.DRIVER_NOT_ELIGIBLE,
                    "Driver must be ACTIVE and KYC VERIFIED to go online");
        }
        boolean hasActive = rideRepository.existsActiveRideForDriver(driver.getId(), RideStatus.activeStatuses());
        if (!goingOnline && hasActive) {
            throw new ForbiddenException("Cannot go offline during an active ride");
        }
        driver.setOnline(goingOnline);
        if (goingOnline) {
            driver.setStatus(DriverStatus.ONLINE);
            driver.setAvailable(true);
            realtimePublisher.driverOnline(driver);
        } else {
            driver.setStatus(DriverStatus.OFFLINE);
            driver.setAvailable(false);
            realtimePublisher.driverOffline(driver);
        }
        log.info("Driver {} availability online={}", driver.getId(), goingOnline);
        return driverMapper.toResponse(driverRepository.save(driver));
    }

    private Driver getDriver(Long userId) {
        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new ForbiddenException("Driver profile not found"));
        if (driver.getVehicle() != null) {
            driver.getVehicle().getVehicleType();
        }
        driver.getUser();
        return driver;
    }
}
