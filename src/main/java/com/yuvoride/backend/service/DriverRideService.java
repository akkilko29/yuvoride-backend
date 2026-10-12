package com.yuvoride.backend.service;

import com.yuvoride.backend.dto.common.PagedResponse;
import com.yuvoride.backend.dto.ride.RideResponse;
import com.yuvoride.backend.enums.RideStatus;
import com.yuvoride.backend.security.UserPrincipal;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DriverRideService {
    List<RideResponse> openRequests(UserPrincipal principal);
    RideResponse active(UserPrincipal principal);
    PagedResponse<RideResponse> history(UserPrincipal principal, Pageable pageable);
    RideResponse get(UserPrincipal principal, Long rideId);
    RideResponse accept(UserPrincipal principal, Long rideId);
    void reject(UserPrincipal principal, Long rideId);
    RideResponse updateStatus(UserPrincipal principal, Long rideId, RideStatus status);
    RideResponse start(UserPrincipal principal, Long rideId);
    RideResponse complete(UserPrincipal principal, Long rideId);
    RideResponse cancel(UserPrincipal principal, Long rideId, String reason);
}
