package com.yuvoride.backend.service;

import com.yuvoride.backend.entity.VehicleType;
import com.yuvoride.backend.service.impl.FareCalculationServiceImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class FareCalculationServiceImplTest {

    private final FareCalculationServiceImpl service = new FareCalculationServiceImpl();

    @Test
    void calculatesFareFromVehicleTypeRates() {
        VehicleType mini = VehicleType.builder()
                .name("Mini")
                .baseFare(new BigDecimal("50"))
                .perKmRate(new BigDecimal("15"))
                .perMinuteRate(new BigDecimal("0"))
                .minimumFare(new BigDecimal("80"))
                .build();

        BigDecimal fare = service.calculate(mini, 65.4, 95);

        assertThat(fare).isEqualByComparingTo("1031");
    }

    @Test
    void appliesMinimumFare() {
        VehicleType mini = VehicleType.builder()
                .name("Mini")
                .baseFare(new BigDecimal("50"))
                .perKmRate(new BigDecimal("15"))
                .perMinuteRate(new BigDecimal("0"))
                .minimumFare(new BigDecimal("80"))
                .build();

        BigDecimal fare = service.calculate(mini, 0.5, 2);

        assertThat(fare).isEqualByComparingTo("80");
    }
}
