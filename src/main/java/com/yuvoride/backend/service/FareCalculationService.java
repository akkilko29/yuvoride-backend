package com.yuvoride.backend.service;

import com.yuvoride.backend.entity.VehicleType;

import java.math.BigDecimal;

public interface FareCalculationService {
    BigDecimal calculate(VehicleType vehicleType, double distanceKm, int durationMinutes);
}
