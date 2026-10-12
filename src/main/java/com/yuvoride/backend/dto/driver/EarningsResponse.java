package com.yuvoride.backend.dto.driver;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class EarningsResponse {
    private final BigDecimal todayEarnings;
    private final long todayRides;
    private final BigDecimal weeklyEarnings;
    private final long totalCompletedRides;
    private final List<EarningRide> rides;

    @Getter
    @Builder
    public static class EarningRide {
        private final Long id;
        private final String date;
        private final String pickup;
        private final String destination;
        private final Long distanceMeters;
        private final BigDecimal fare;
        private final BigDecimal driverEarning;
    }
}
