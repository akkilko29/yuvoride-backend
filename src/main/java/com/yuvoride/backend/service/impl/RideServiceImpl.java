package com.yuvoride.backend.service.impl;

import com.yuvoride.backend.config.AppProperties;
import com.yuvoride.backend.dto.common.PagedResponse;
import com.yuvoride.backend.dto.ride.CreateRideRequest;
import com.yuvoride.backend.dto.ride.RideEstimateRequest;
import com.yuvoride.backend.dto.ride.RideEstimateResponse;
import com.yuvoride.backend.dto.ride.RideResponse;
import com.yuvoride.backend.entity.Driver;
import com.yuvoride.backend.entity.Ride;
import com.yuvoride.backend.entity.User;
import com.yuvoride.backend.entity.VehicleType;
import com.yuvoride.backend.enums.DriverStatus;
import com.yuvoride.backend.enums.RideStatus;
import com.yuvoride.backend.enums.UserRole;
import com.yuvoride.backend.exception.BadRequestException;
import com.yuvoride.backend.exception.ForbiddenException;
import com.yuvoride.backend.exception.ResourceNotFoundException;
import com.yuvoride.backend.exception.RideStateException;
import com.yuvoride.backend.mapper.RideMapper;
import com.yuvoride.backend.repository.DriverRepository;
import com.yuvoride.backend.repository.RideRepository;
import com.yuvoride.backend.repository.UserRepository;
import com.yuvoride.backend.security.UserPrincipal;
import com.yuvoride.backend.service.DriverMatchingService;
import com.yuvoride.backend.service.FareCalculationService;
import com.yuvoride.backend.service.RideService;
import com.yuvoride.backend.service.RideStateMachine;
import com.yuvoride.backend.service.RouteService;
import com.yuvoride.backend.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RideServiceImpl implements RideService {

    private static final List<RideStatus> ACTIVE_CUSTOMER_STATUSES = List.of(
            RideStatus.REQUESTED,
            RideStatus.SEARCHING_DRIVER,
            RideStatus.DRIVER_ASSIGNED,
            RideStatus.DRIVER_ARRIVING,
            RideStatus.DRIVER_ARRIVED,
            RideStatus.RIDE_STARTED
    );

    private final VehicleService vehicleService;
    private final RouteService routeService;
    private final FareCalculationService fareCalculationService;
    private final RideRepository rideRepository;
    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final DriverMatchingService driverMatchingService;
    private final RideStateMachine rideStateMachine;
    private final RideMapper rideMapper;
    private final AppProperties properties;

    @Override
    public RideEstimateResponse estimate(RideEstimateRequest request) {
        VehicleType vehicleType = vehicleService.getActive(request.getVehicleTypeId());
        RouteService.RouteResult route = routeService.getRoute(
                request.getPickup().getLatitude(),
                request.getPickup().getLongitude(),
                request.getDestination().getLatitude(),
                request.getDestination().getLongitude()
        );
        BigDecimal fare = fareCalculationService.calculate(vehicleType, route.distanceKm(), route.durationMinutes());
        return RideEstimateResponse.builder()
                .distanceKm(route.distanceKm())
                .durationMinutes(route.durationMinutes())
                .estimatedFare(fare)
                .vehicleType(vehicleType.getName())
                .currency(properties.getCurrency())
                .build();
    }

    @Override
    @Transactional
    public RideResponse create(UserPrincipal principal, CreateRideRequest request) {
        User customer = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (customer.getRole() != UserRole.CUSTOMER && customer.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("Only customers can create rides");
        }
        if (customer.getPhone() == null) {
            throw new BadRequestException("Add a phone number before booking a ride");
        }
        if (rideRepository.existsActiveRideForCustomer(customer.getId(), ACTIVE_CUSTOMER_STATUSES)) {
            throw new BadRequestException("You already have an active ride");
        }
        if (request.getScheduledAt() != null && request.getScheduledAt().isBefore(Instant.now())) {
            throw new BadRequestException("scheduledAt must be in the future");
        }

        VehicleType vehicleType = vehicleService.getActive(request.getVehicleTypeId());
        RouteService.RouteResult route = routeService.getRoute(
                request.getPickup().getLatitude(),
                request.getPickup().getLongitude(),
                request.getDestination().getLatitude(),
                request.getDestination().getLongitude()
        );
        BigDecimal fare = fareCalculationService.calculate(vehicleType, route.distanceKm(), route.durationMinutes());

        Ride ride = Ride.builder()
                .customer(customer)
                .pickupLatitude(request.getPickup().getLatitude())
                .pickupLongitude(request.getPickup().getLongitude())
                .pickupAddress(request.getPickup().getAddress())
                .destinationLatitude(request.getDestination().getLatitude())
                .destinationLongitude(request.getDestination().getLongitude())
                .destinationAddress(request.getDestination().getAddress())
                .vehicleType(vehicleType)
                .distanceKm(route.distanceKm())
                .durationMinutes(route.durationMinutes())
                .estimatedFare(fare)
                .status(RideStatus.REQUESTED)
                .scheduledAt(request.getScheduledAt())
                .build();
        ride = rideRepository.save(ride);
        driverMatchingService.assignDriver(ride);
        return rideMapper.toResponse(rideRepository.save(ride));
    }

    @Override
    @Transactional(readOnly = true)
    public RideResponse getById(UserPrincipal principal, Long rideId) {
        return rideMapper.toResponse(loadAuthorized(principal, rideId));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<RideResponse> history(UserPrincipal principal, RideStatus status, Pageable pageable) {
        Page<Ride> page;
        if (principal.getRole() == UserRole.DRIVER) {
            Driver driver = driverRepository.findByUserId(principal.getId())
                    .orElseThrow(() -> new ForbiddenException("Driver profile not found"));
            page = status == null
                    ? rideRepository.findByDriverId(driver.getId(), pageable)
                    : rideRepository.findByDriverIdAndStatus(driver.getId(), status, pageable);
        } else if (principal.getRole() == UserRole.ADMIN) {
            page = status == null ? rideRepository.findAll(pageable) : rideRepository.findByStatus(status, pageable);
        } else {
            page = status == null
                    ? rideRepository.findByCustomerId(principal.getId(), pageable)
                    : rideRepository.findByCustomerIdAndStatus(principal.getId(), status, pageable);
        }
        return PagedResponse.from(page.map(rideMapper::toResponse));
    }

    @Override
    @Transactional
    public RideResponse cancel(UserPrincipal principal, Long rideId, String reason) {
        Ride ride = loadAuthorized(principal, rideId);
        if (principal.getRole() == UserRole.DRIVER && ride.getDriver() != null
                && !ride.getDriver().getUser().getId().equals(principal.getId())) {
            throw new ForbiddenException("You cannot cancel this ride");
        }
        if (!rideStateMachine.canCancel(ride.getStatus())) {
            throw new RideStateException("Ride cannot be cancelled in status " + ride.getStatus());
        }
        rideStateMachine.assertTransition(ride.getStatus(), RideStatus.CANCELLED);
        ride.setStatus(RideStatus.CANCELLED);
        ride.setCancelledAt(Instant.now());
        ride.setCancellationReason(reason);
        releaseDriver(ride);
        return rideMapper.toResponse(rideRepository.save(ride));
    }

    @Override
    @Transactional
    public RideResponse driverEnRoute(UserPrincipal principal, Long rideId) {
        return transitionByDriver(principal, rideId, RideStatus.DRIVER_ARRIVING);
    }

    @Override
    @Transactional
    public RideResponse driverArrive(UserPrincipal principal, Long rideId) {
        return transitionByDriver(principal, rideId, RideStatus.DRIVER_ARRIVED);
    }

    @Override
    @Transactional
    public RideResponse driverStart(UserPrincipal principal, Long rideId) {
        Ride ride = requireAssignedDriver(principal, rideId);
        rideStateMachine.assertTransition(ride.getStatus(), RideStatus.RIDE_STARTED);
        ride.setStatus(RideStatus.RIDE_STARTED);
        ride.setStartedAt(Instant.now());
        return rideMapper.toResponse(rideRepository.save(ride));
    }

    @Override
    @Transactional
    public RideResponse driverComplete(UserPrincipal principal, Long rideId) {
        Ride ride = requireAssignedDriver(principal, rideId);
        rideStateMachine.assertTransition(ride.getStatus(), RideStatus.RIDE_COMPLETED);
        ride.setStatus(RideStatus.RIDE_COMPLETED);
        ride.setCompletedAt(Instant.now());
        ride.setFinalFare(ride.getEstimatedFare());
        if (ride.getDriver() != null) {
            Driver driver = ride.getDriver();
            driver.setAvailable(true);
            driver.setOnline(true);
            driver.setStatus(DriverStatus.ONLINE);
            driver.setTotalRides(driver.getTotalRides() + 1);
            driverRepository.save(driver);
        }
        return rideMapper.toResponse(rideRepository.save(ride));
    }

    private RideResponse transitionByDriver(UserPrincipal principal, Long rideId, RideStatus target) {
        Ride ride = requireAssignedDriver(principal, rideId);
        rideStateMachine.assertTransition(ride.getStatus(), target);
        ride.setStatus(target);
        return rideMapper.toResponse(rideRepository.save(ride));
    }

    private Ride requireAssignedDriver(UserPrincipal principal, Long rideId) {
        if (principal.getRole() != UserRole.DRIVER && principal.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("Only the assigned driver can update this ride");
        }
        Ride ride = loadAuthorized(principal, rideId);
        if (principal.getRole() == UserRole.DRIVER) {
            Driver driver = driverRepository.findByUserId(principal.getId())
                    .orElseThrow(() -> new ForbiddenException("Driver profile not found"));
            if (ride.getDriver() == null || !ride.getDriver().getId().equals(driver.getId())) {
                throw new ForbiddenException("This ride is not assigned to you");
            }
        }
        return ride;
    }

    private Ride loadAuthorized(UserPrincipal principal, Long rideId) {
        Ride ride = rideRepository.findDetailedById(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found"));
        if (principal.getRole() == UserRole.ADMIN) {
            return ride;
        }
        if (principal.getRole() == UserRole.DRIVER) {
            if (ride.getDriver() == null || !ride.getDriver().getUser().getId().equals(principal.getId())) {
                throw new ForbiddenException("You cannot access this ride");
            }
            return ride;
        }
        if (!ride.getCustomer().getId().equals(principal.getId())) {
            throw new ForbiddenException("You cannot access this ride");
        }
        return ride;
    }

    private void releaseDriver(Ride ride) {
        if (ride.getDriver() == null) {
            return;
        }
        Driver driver = ride.getDriver();
        driver.setAvailable(true);
        if (driver.getStatus() != DriverStatus.SUSPENDED) {
            driver.setStatus(driver.isOnline() ? DriverStatus.ONLINE : DriverStatus.OFFLINE);
        }
        driverRepository.save(driver);
    }
}
