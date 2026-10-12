package com.yuvoride.backend.service.impl;

import com.yuvoride.backend.entity.VehicleType;
import com.yuvoride.backend.service.FareCalculationService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class FareCalculationServiceImpl implements FareCalculationService {

    @Override
    public BigDecimal calculate(VehicleType vehicleType, double distanceKm, int durationMinutes) {
        BigDecimal fare = vehicleType.getBaseFare()
                .add(vehicleType.getPerKmRate().multiply(BigDecimal.valueOf(distanceKm)))
                .add(vehicleType.getPerMinuteRate().multiply(BigDecimal.valueOf(durationMinutes)));
        if (fare.compareTo(vehicleType.getMinimumFare()) < 0) {
            fare = vehicleType.getMinimumFare();
        }
        return fare.setScale(0, RoundingMode.HALF_UP);
    }
}
