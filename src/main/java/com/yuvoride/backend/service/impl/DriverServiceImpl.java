package com.yuvoride.backend.service.impl;

import com.yuvoride.backend.dto.driver.DriverResponse;
import com.yuvoride.backend.dto.driver.UpdateDriverAvailabilityRequest;
import com.yuvoride.backend.dto.driver.UpdateDriverLocationRequest;
import com.yuvoride.backend.entity.Driver;
import com.yuvoride.backend.enums.DriverStatus;
import com.yuvoride.backend.exception.ForbiddenException;
import com.yuvoride.backend.mapper.DriverMapper;
import com.yuvoride.backend.repository.DriverRepository;
import com.yuvoride.backend.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final DriverMapper driverMapper;

    @Override
    @Transactional(readOnly = true)
    public DriverResponse me(Long userId) {
        return driverMapper.toResponse(getDriver(userId));
    }

    @Override
    @Transactional
    public DriverResponse updateLocation(Long userId, UpdateDriverLocationRequest request) {
        Driver driver = getDriver(userId);
        driver.setCurrentLatitude(request.getLatitude());
        driver.setCurrentLongitude(request.getLongitude());
        return driverMapper.toResponse(driverRepository.save(driver));
    }

    @Override
    @Transactional
    public DriverResponse updateAvailability(Long userId, UpdateDriverAvailabilityRequest request) {
        Driver driver = getDriver(userId);
        if (driver.getStatus() == DriverStatus.SUSPENDED) {
            throw new ForbiddenException("Driver account is suspended");
        }
        if (driver.getStatus() == DriverStatus.BUSY && Boolean.FALSE.equals(request.getOnline())) {
            throw new ForbiddenException("Cannot go offline during an active ride");
        }
        driver.setOnline(request.getOnline());
        if (Boolean.TRUE.equals(request.getOnline()) && driver.getStatus() != DriverStatus.BUSY) {
            driver.setStatus(DriverStatus.ONLINE);
            driver.setAvailable(true);
        } else if (Boolean.FALSE.equals(request.getOnline()) && driver.getStatus() != DriverStatus.BUSY) {
            driver.setStatus(DriverStatus.OFFLINE);
            driver.setAvailable(false);
        }
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
