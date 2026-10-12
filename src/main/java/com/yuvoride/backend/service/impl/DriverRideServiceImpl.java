package com.yuvoride.backend.service.impl;

import com.yuvoride.backend.config.AppProperties;
import com.yuvoride.backend.dto.common.PagedResponse;
import com.yuvoride.backend.dto.ride.RideResponse;
import com.yuvoride.backend.entity.Driver;
import com.yuvoride.backend.entity.Ride;
import com.yuvoride.backend.enums.DriverStatus;
import com.yuvoride.backend.enums.RideStatus;
import com.yuvoride.backend.exception.ConflictException;
import com.yuvoride.backend.exception.ErrorCodes;
import com.yuvoride.backend.exception.ForbiddenException;
import com.yuvoride.backend.exception.ResourceNotFoundException;
import com.yuvoride.backend.exception.UnprocessableException;
import com.yuvoride.backend.mapper.RideMapper;
import com.yuvoride.backend.repository.DriverRepository;
import com.yuvoride.backend.repository.RideRepository;
import com.yuvoride.backend.security.UserPrincipal;
import com.yuvoride.backend.service.DriverRideService;
import com.yuvoride.backend.service.NotificationService;
import com.yuvoride.backend.service.RideService;
import com.yuvoride.backend.service.RideStateMachine;
import com.yuvoride.backend.util.GeoUtils;
import com.yuvoride.backend.websocket.RealtimePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverRideServiceImpl implements DriverRideService {

    private static final Set<RideStatus> OPEN = Set.of(RideStatus.REQUESTED, RideStatus.SEARCHING_DRIVER);

    private final DriverRepository driverRepository;
    private final RideRepository rideRepository;
    private final RideMapper rideMapper;
    private final RideStateMachine rideStateMachine;
    private final RideService rideService;
    private final AppProperties properties;
    private final RealtimePublisher realtimePublisher;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public List<RideResponse> openRequests(UserPrincipal principal) {
        Driver driver = requireEligible(principal);
        Long vehicleTypeId = resolveVehicleTypeId(driver);
        if (vehicleTypeId == null) {
            return List.of();
        }
        double radius = properties.getDriverMatch().getRadiusKm();
        return rideRepository.findOpenRequests(vehicleTypeId, OPEN, Instant.now()).stream()
                .filter(ride -> driver.getCurrentLatitude() == null
                        || GeoUtils.haversineKm(ride.getPickupLatitude(), ride.getPickupLongitude(),
                        driver.getCurrentLatitude(), driver.getCurrentLongitude()) <= radius)
                .map(rideMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RideResponse active(UserPrincipal principal) {
        Driver driver = requireDriver(principal);
        return rideRepository.findFirstByDriverIdAndStatusInOrderByCreatedAtDesc(driver.getId(), RideStatus.activeStatuses())
                .map(rideMapper::toResponse)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<RideResponse> history(UserPrincipal principal, Pageable pageable) {
        return rideService.history(principal, null, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public RideResponse get(UserPrincipal principal, Long rideId) {
        Driver driver = requireDriver(principal);
        Ride ride = rideRepository.findDetailedById(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found"));
        boolean assigned = ride.getDriver() != null && ride.getDriver().getId().equals(driver.getId());
        boolean open = OPEN.contains(ride.getStatus());
        if (!assigned && !open) {
            throw new ForbiddenException("You cannot access this ride");
        }
        return rideMapper.toResponse(ride);
    }

    @Override
    @Transactional
    public RideResponse accept(UserPrincipal principal, Long rideId) {
        Driver driver = requireEligible(principal);
        if (rideRepository.existsActiveRideForDriver(driver.getId(), RideStatus.activeStatuses())) {
            throw new UnprocessableException(ErrorCodes.DRIVER_NOT_ELIGIBLE, "You already have an active ride");
        }
        Ride ride = rideRepository.findByIdForUpdate(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found"));
        RideStatus canonical = ride.getStatus().canonical();
        if (canonical == RideStatus.EXPIRED || (ride.getExpiresAt() != null && ride.getExpiresAt().isBefore(Instant.now()))) {
            ride.setStatus(RideStatus.EXPIRED);
            rideRepository.save(ride);
            throw new ConflictException(ErrorCodes.INVALID_RIDE_STATE, "This ride request has expired");
        }
        if (canonical != RideStatus.REQUESTED) {
            throw new ConflictException(ErrorCodes.RIDE_ALREADY_TAKEN,
                    "This ride has already been accepted by another driver.");
        }
        rideStateMachine.assertTransition(ride.getStatus(), RideStatus.ACCEPTED);
        ride.setDriver(driver);
        ride.setStatus(RideStatus.ACCEPTED);
        ride.setAcceptedAt(Instant.now());
        driver.setAvailable(false);
        driver.setStatus(DriverStatus.BUSY);
        driverRepository.save(driver);
        rideRepository.save(ride);
        Ride detailed = rideRepository.findDetailedById(ride.getId()).orElse(ride);
        realtimePublisher.rideAccepted(detailed);
        notificationService.notifyCustomerDriverAssigned(detailed.getCustomer(), detailed);
        log.info("Driver {} accepted ride {}", driver.getId(), ride.getId());
        return rideMapper.toResponse(detailed);
    }

    @Override
    public void reject(UserPrincipal principal, Long rideId) {
        requireDriver(principal);
        log.info("Driver {} rejected ride {}", principal.getId(), rideId);
    }

    @Override
    @Transactional
    public RideResponse updateStatus(UserPrincipal principal, Long rideId, RideStatus status) {
        return switch (status.canonical()) {
            case GOING_TO_PICKUP -> rideService.driverEnRoute(principal, rideId);
            case ARRIVED -> rideService.driverArrive(principal, rideId);
            case RIDE_STARTED -> rideService.driverStart(principal, rideId);
            case GOING_TO_DESTINATION -> transitionGoingToDestination(principal, rideId);
            case COMPLETED -> rideService.driverComplete(principal, rideId);
            case RIDE_CANCELLED -> rideService.cancel(principal, rideId, "Cancelled by driver");
            default -> throw new UnprocessableException(ErrorCodes.INVALID_RIDE_STATE, "Unsupported status update");
        };
    }

    @Override
    public RideResponse start(UserPrincipal principal, Long rideId) {
        return rideService.driverStart(principal, rideId);
    }

    @Override
    public RideResponse complete(UserPrincipal principal, Long rideId) {
        return rideService.driverComplete(principal, rideId);
    }

    @Override
    public RideResponse cancel(UserPrincipal principal, Long rideId, String reason) {
        return rideService.cancel(principal, rideId, reason);
    }

    private RideResponse transitionGoingToDestination(UserPrincipal principal, Long rideId) {
        Ride ride = rideRepository.findDetailedById(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found"));
        Driver driver = requireDriver(principal);
        if (ride.getDriver() == null || !ride.getDriver().getId().equals(driver.getId())) {
            throw new ForbiddenException("This ride is not assigned to you");
        }
        rideStateMachine.assertTransition(ride.getStatus(), RideStatus.GOING_TO_DESTINATION);
        ride.setStatus(RideStatus.GOING_TO_DESTINATION);
        rideRepository.save(ride);
        realtimePublisher.rideStatusChanged(ride, "ride:status_changed");
        return rideMapper.toResponse(ride);
    }

    private Driver requireEligible(UserPrincipal principal) {
        Driver driver = requireDriver(principal);
        if (!driver.canGoOnline() || !driver.isOnline() || !driver.isAvailable()) {
            throw new UnprocessableException(ErrorCodes.DRIVER_NOT_ELIGIBLE,
                    "Driver must be ACTIVE, KYC VERIFIED, online, and available");
        }
        return driver;
    }

    private Driver requireDriver(UserPrincipal principal) {
        return driverRepository.findByUserId(principal.getId())
                .orElseThrow(() -> new ForbiddenException("Driver profile not found"));
    }

    private Long resolveVehicleTypeId(Driver driver) {
        if (driver.getPreferredVehicleType() != null) {
            return driver.getPreferredVehicleType().getId();
        }
        if (driver.getVehicle() != null && driver.getVehicle().getVehicleType() != null) {
            return driver.getVehicle().getVehicleType().getId();
        }
        return null;
    }
}
