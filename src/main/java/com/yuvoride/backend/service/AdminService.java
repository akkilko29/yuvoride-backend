package com.yuvoride.backend.service;

import com.yuvoride.backend.dto.admin.AdminDriverStatusRequest;
import com.yuvoride.backend.dto.admin.AdminKycRequest;
import com.yuvoride.backend.dto.driver.DriverResponse;
import com.yuvoride.backend.dto.ride.RideResponse;
import com.yuvoride.backend.dto.user.UserResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminService {
    List<DriverResponse> listDrivers();
    DriverResponse getDriver(Long id);
    DriverResponse updateKyc(Long id, AdminKycRequest request);
    DriverResponse updateStatus(Long id, AdminDriverStatusRequest request);
    List<UserResponse> listUsers(Pageable pageable);
    List<UserResponse> listCustomers(Pageable pageable);
    List<RideResponse> listRides(Pageable pageable);
}
