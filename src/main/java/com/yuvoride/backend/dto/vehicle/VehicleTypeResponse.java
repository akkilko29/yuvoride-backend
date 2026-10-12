package com.yuvoride.backend.dto.vehicle;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class VehicleTypeResponse {
    private final Long id;
    private final String name;
    private final String description;
    private final Integer capacity;
    private final BigDecimal baseFare;
    private final BigDecimal perKmRate;
    private final BigDecimal perMinuteRate;
    private final BigDecimal minimumFare;
}
