package com.yuvoride.backend.service;

import com.yuvoride.backend.dto.vehicle.VehicleTypeResponse;
import com.yuvoride.backend.entity.VehicleType;

import java.util.List;

public interface VehicleService {
    List<VehicleTypeResponse> listActive();
    VehicleType getActive(Long id);
}
