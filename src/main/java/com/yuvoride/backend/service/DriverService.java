package com.yuvoride.backend.service;

import com.yuvoride.backend.dto.driver.DriverLocationRequest;
import com.yuvoride.backend.dto.driver.DriverResponse;
import com.yuvoride.backend.dto.driver.KycResponse;
import com.yuvoride.backend.dto.driver.PatchDriverRequest;
import com.yuvoride.backend.dto.driver.UpdateDriverAvailabilityRequest;
import com.yuvoride.backend.dto.driver.UpdateDriverLocationRequest;
import com.yuvoride.backend.dto.driver.UpdateKycRequest;

public interface DriverService {
    DriverResponse me(Long userId);
    DriverResponse updateProfile(Long userId, PatchDriverRequest request);
    DriverResponse updateLocation(Long userId, UpdateDriverLocationRequest request);
    DriverResponse updateLocation(Long userId, DriverLocationRequest request);
    DriverResponse updateAvailability(Long userId, UpdateDriverAvailabilityRequest request);
    KycResponse getKyc(Long userId);
    KycResponse updateKyc(Long userId, UpdateKycRequest request);
}
