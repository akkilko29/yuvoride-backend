package com.yuvoride.backend.service.impl;

import com.yuvoride.backend.entity.VehicleType;
import com.yuvoride.backend.service.FareCalculationService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class FareCalculationServiceImpl implements FareCalculationService {

    private static final BigDecimal DEFAULT_COMMISSION = new BigDecimal("20");

    @Override
    public BigDecimal calculate(VehicleType vehicleType, double distanceKm, int durationMinutes) {
        return quote(vehicleType, distanceKm, durationMinutes).estimatedFare();
    }

    @Override
    public FareQuote quote(VehicleType vehicleType, double distanceKm, int durationMinutes) {
        BigDecimal fare = vehicleType.getBaseFare()
                .add(vehicleType.getPerKmRate().multiply(BigDecimal.valueOf(distanceKm)))
                .add(vehicleType.getPerMinuteRate().multiply(BigDecimal.valueOf(durationMinutes)));
        if (fare.compareTo(vehicleType.getMinimumFare()) < 0) {
            fare = vehicleType.getMinimumFare();
        }
        fare = fare.setScale(0, RoundingMode.HALF_UP);
        BigDecimal percent = vehicleType.getCommissionPercent() != null
                ? vehicleType.getCommissionPercent()
                : DEFAULT_COMMISSION;
        BigDecimal commission = fare.multiply(percent).divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);
        BigDecimal earning = fare.subtract(commission).max(BigDecimal.ZERO);
        return new FareQuote(fare, commission, earning);
    }
}
