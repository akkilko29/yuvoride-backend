package com.yuvoride.backend.dto.ride;

import com.yuvoride.backend.dto.common.LocationDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RideEstimateRequest {

    @NotNull(message = "Pickup is required")
    @Valid
    private LocationDto pickup;

    @NotNull(message = "Destination is required")
    @Valid
    private LocationDto destination;

    private Long vehicleTypeId;
    private String vehicleType;

    @AssertTrue(message = "Vehicle type is required")
    public boolean isVehicleSpecified() {
        return vehicleTypeId != null || (vehicleType != null && !vehicleType.isBlank());
    }
}
