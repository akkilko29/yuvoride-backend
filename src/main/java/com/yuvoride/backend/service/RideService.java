package com.yuvoride.backend.service;

import com.yuvoride.backend.dto.common.PagedResponse;
import com.yuvoride.backend.dto.ride.CreateRideRequest;
import com.yuvoride.backend.dto.ride.RideEstimateRequest;
import com.yuvoride.backend.dto.ride.RideEstimateResponse;
import com.yuvoride.backend.dto.ride.RideResponse;
import com.yuvoride.backend.enums.RideStatus;
import com.yuvoride.backend.security.UserPrincipal;
import org.springframework.data.domain.Pageable;

public interface RideService {
    RideEstimateResponse estimate(RideEstimateRequest request);
    RideResponse create(UserPrincipal principal, CreateRideRequest request);
    RideResponse getById(UserPrincipal principal, Long rideId);
    PagedResponse<RideResponse> history(UserPrincipal principal, RideStatus status, Pageable pageable);
    RideResponse cancel(UserPrincipal principal, Long rideId, String reason);
    RideResponse driverArrive(UserPrincipal principal, Long rideId);
    RideResponse driverStart(UserPrincipal principal, Long rideId);
    RideResponse driverComplete(UserPrincipal principal, Long rideId);
    RideResponse driverEnRoute(UserPrincipal principal, Long rideId);
}
