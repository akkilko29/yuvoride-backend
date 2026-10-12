package com.yuvoride.backend.controller;

import com.yuvoride.backend.dto.common.ApiResponse;
import com.yuvoride.backend.dto.vehicle.VehicleTypeResponse;
import com.yuvoride.backend.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/vehicles")
@Tag(name = "Vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping
    @Operation(summary = "List active vehicle types and fares")
    public ResponseEntity<ApiResponse<List<VehicleTypeResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.ok("Vehicle types loaded", vehicleService.listActive()));
    }
}
