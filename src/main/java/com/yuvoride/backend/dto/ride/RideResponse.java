package com.yuvoride.backend.dto.ride;

import com.yuvoride.backend.dto.common.LocationDto;
import com.yuvoride.backend.dto.user.UserResponse;
import com.yuvoride.backend.enums.RideStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
public class RideResponse {
    private final Long id;
    private final UserResponse customer;
    private final Long driverId;
    private final LocationDto pickup;
    private final LocationDto destination;
    private final String vehicleType;
    private final Long vehicleTypeId;
    private final Double distanceKm;
    private final Integer durationMinutes;
    private final Long distanceMeters;
    private final Integer durationSeconds;
    private final Integer etaSeconds;
    private final BigDecimal estimatedFare;
    private final BigDecimal finalFare;
    private final BigDecimal driverEarning;
    private final BigDecimal commission;
    private final RideStatus status;
    private final Instant scheduledAt;
    private final Instant requestedAt;
    private final Instant acceptedAt;
    private final Instant arrivedAt;
    private final Instant startedAt;
    private final Instant completedAt;
    private final Instant cancelledAt;
    private final String cancellationReason;
    private final Instant createdAt;
    private final Instant updatedAt;
}
