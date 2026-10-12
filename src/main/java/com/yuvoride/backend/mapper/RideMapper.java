package com.yuvoride.backend.mapper;

import com.yuvoride.backend.dto.common.LocationDto;
import com.yuvoride.backend.dto.ride.RideResponse;
import com.yuvoride.backend.entity.Ride;
import org.springframework.stereotype.Component;

@Component
public class RideMapper {

    private final UserMapper userMapper;

    public RideMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public RideResponse toResponse(Ride ride) {
        LocationDto pickup = new LocationDto();
        pickup.setLatitude(ride.getPickupLatitude());
        pickup.setLongitude(ride.getPickupLongitude());
        pickup.setAddress(ride.getPickupAddress());

        LocationDto destination = new LocationDto();
        destination.setLatitude(ride.getDestinationLatitude());
        destination.setLongitude(ride.getDestinationLongitude());
        destination.setAddress(ride.getDestinationAddress());

        return RideResponse.builder()
                .id(ride.getId())
                .customer(userMapper.toResponse(ride.getCustomer()))
                .driverId(ride.getDriver() != null ? ride.getDriver().getId() : null)
                .pickup(pickup)
                .destination(destination)
                .vehicleType(ride.getVehicleType().getName())
                .vehicleTypeId(ride.getVehicleType().getId())
                .distanceKm(ride.getDistanceKm())
                .durationMinutes(ride.getDurationMinutes())
                .estimatedFare(ride.getEstimatedFare())
                .finalFare(ride.getFinalFare())
                .status(ride.getStatus())
                .scheduledAt(ride.getScheduledAt())
                .startedAt(ride.getStartedAt())
                .completedAt(ride.getCompletedAt())
                .cancelledAt(ride.getCancelledAt())
                .cancellationReason(ride.getCancellationReason())
                .createdAt(ride.getCreatedAt())
                .updatedAt(ride.getUpdatedAt())
                .build();
    }
}
