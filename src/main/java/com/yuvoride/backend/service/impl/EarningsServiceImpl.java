package com.yuvoride.backend.service.impl;

import com.yuvoride.backend.dto.driver.EarningsResponse;
import com.yuvoride.backend.entity.Driver;
import com.yuvoride.backend.entity.Ride;
import com.yuvoride.backend.enums.RideStatus;
import com.yuvoride.backend.exception.ForbiddenException;
import com.yuvoride.backend.repository.DriverRepository;
import com.yuvoride.backend.repository.RideRepository;
import com.yuvoride.backend.service.EarningsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EarningsServiceImpl implements EarningsService {

    private final DriverRepository driverRepository;
    private final RideRepository rideRepository;

    @Override
    @Transactional(readOnly = true)
    public EarningsResponse forDriver(Long userId) {
        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new ForbiddenException("Driver profile not found"));
        Instant startOfDay = ZonedDateTime.now(ZoneOffset.UTC).toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant startOfWeek = startOfDay.minusSeconds(6L * 24 * 3600);

        List<Ride> today = rideRepository.findByDriverIdAndStatusAndCompletedAtGreaterThanEqual(
                driver.getId(), RideStatus.COMPLETED, startOfDay);
        List<Ride> alsoCompleted = rideRepository.findByDriverIdAndStatusAndCompletedAtGreaterThanEqual(
                driver.getId(), RideStatus.RIDE_COMPLETED, startOfDay);
        today = concat(today, alsoCompleted);

        List<Ride> week = concat(
                rideRepository.findByDriverIdAndStatusAndCompletedAtGreaterThanEqual(driver.getId(), RideStatus.COMPLETED, startOfWeek),
                rideRepository.findByDriverIdAndStatusAndCompletedAtGreaterThanEqual(driver.getId(), RideStatus.RIDE_COMPLETED, startOfWeek)
        );

        long total = rideRepository.findByDriverIdAndStatus(driver.getId(), RideStatus.COMPLETED, PageRequest.of(0, 1)).getTotalElements()
                + rideRepository.findByDriverIdAndStatus(driver.getId(), RideStatus.RIDE_COMPLETED, PageRequest.of(0, 1)).getTotalElements();

        List<Ride> recent = rideRepository.findByDriverId(driver.getId(), PageRequest.of(0, 20)).getContent();
        return EarningsResponse.builder()
                .todayEarnings(sum(today))
                .todayRides(today.size())
                .weeklyEarnings(sum(week))
                .totalCompletedRides(total)
                .rides(recent.stream()
                        .filter(ride -> ride.getStatus().canonical() == RideStatus.COMPLETED)
                        .map(this::toItem)
                        .toList())
                .build();
    }

    private EarningsResponse.EarningRide toItem(Ride ride) {
        Instant when = ride.getCompletedAt() != null ? ride.getCompletedAt() : ride.getCreatedAt();
        return EarningsResponse.EarningRide.builder()
                .id(ride.getId())
                .date(when != null ? DateTimeFormatter.ISO_INSTANT.format(when) : null)
                .pickup(ride.getPickupAddress())
                .destination(ride.getDestinationAddress())
                .distanceMeters(ride.getDistanceMeters())
                .fare(ride.getFinalFare() != null ? ride.getFinalFare() : ride.getEstimatedFare())
                .driverEarning(ride.getDriverEarning())
                .build();
    }

    private BigDecimal sum(List<Ride> rides) {
        return rides.stream()
                .map(ride -> ride.getDriverEarning() != null ? ride.getDriverEarning() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<Ride> concat(List<Ride> a, List<Ride> b) {
        return java.util.stream.Stream.concat(a.stream(), b.stream()).distinct().toList();
    }
}
