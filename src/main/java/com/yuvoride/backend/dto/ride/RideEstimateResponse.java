package com.yuvoride.backend.dto.ride;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class RideEstimateResponse {
    private final Double distanceKm;
    private final Integer durationMinutes;
    private final BigDecimal estimatedFare;
    private final String vehicleType;
    private final String currency;
}
