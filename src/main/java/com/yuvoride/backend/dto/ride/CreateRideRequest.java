package com.yuvoride.backend.dto.ride;

import com.yuvoride.backend.dto.common.LocationDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class CreateRideRequest {

    @NotNull(message = "Pickup is required")
    @Valid
    private LocationDto pickup;

    @NotNull(message = "Destination is required")
    @Valid
    private LocationDto destination;

    private Long vehicleTypeId;
    private String vehicleType;
    private Instant scheduledAt;

    @AssertTrue(message = "Vehicle type is required")
    public boolean isVehicleSpecified() {
        return vehicleTypeId != null || (vehicleType != null && !vehicleType.isBlank());
    }
}
