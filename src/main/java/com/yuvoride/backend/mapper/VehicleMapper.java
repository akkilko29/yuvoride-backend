package com.yuvoride.backend.mapper;

import com.yuvoride.backend.dto.vehicle.VehicleTypeResponse;
import com.yuvoride.backend.entity.VehicleType;
import org.springframework.stereotype.Component;

@Component
public class VehicleMapper {

    public VehicleTypeResponse toResponse(VehicleType type) {
        return VehicleTypeResponse.builder()
                .id(type.getId())
                .name(type.getName())
                .description(type.getDescription())
                .capacity(type.getCapacity())
                .baseFare(type.getBaseFare())
                .perKmRate(type.getPerKmRate())
                .perMinuteRate(type.getPerMinuteRate())
                .minimumFare(type.getMinimumFare())
                .build();
    }
}
