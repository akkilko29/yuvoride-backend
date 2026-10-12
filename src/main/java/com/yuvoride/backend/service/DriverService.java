package com.yuvoride.backend.service;

import com.yuvoride.backend.dto.driver.DriverResponse;
import com.yuvoride.backend.dto.driver.UpdateDriverAvailabilityRequest;
import com.yuvoride.backend.dto.driver.UpdateDriverLocationRequest;

public interface DriverService {
    DriverResponse me(Long userId);
    DriverResponse updateLocation(Long userId, UpdateDriverLocationRequest request);
    DriverResponse updateAvailability(Long userId, UpdateDriverAvailabilityRequest request);
}
