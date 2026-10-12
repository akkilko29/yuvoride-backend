package com.yuvoride.backend.dto.ride;

import com.yuvoride.backend.dto.common.LocationDto;
import jakarta.validation.Valid;
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

    @NotNull(message = "Vehicle type is required")
    private Long vehicleTypeId;
}
