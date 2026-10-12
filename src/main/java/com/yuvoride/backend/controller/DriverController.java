package com.yuvoride.backend.controller;

import com.yuvoride.backend.dto.common.ApiResponse;
import com.yuvoride.backend.dto.driver.DriverResponse;
import com.yuvoride.backend.dto.driver.UpdateDriverAvailabilityRequest;
import com.yuvoride.backend.dto.driver.UpdateDriverLocationRequest;
import com.yuvoride.backend.security.CurrentUser;
import com.yuvoride.backend.security.UserPrincipal;
import com.yuvoride.backend.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/drivers")
@Tag(name = "Drivers")
@SecurityRequirement(name = "bearerAuth")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get current driver profile")
    public ResponseEntity<ApiResponse<DriverResponse>> me(@CurrentUser UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Driver profile loaded", driverService.me(principal.getId())));
    }

    @PutMapping("/me/location")
    @Operation(summary = "Update driver location")
    public ResponseEntity<ApiResponse<DriverResponse>> location(@CurrentUser UserPrincipal principal,
                                                                @Valid @RequestBody UpdateDriverLocationRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Location updated", driverService.updateLocation(principal.getId(), request)));
    }

    @PatchMapping("/me/availability")
    @Operation(summary = "Go online or offline")
    public ResponseEntity<ApiResponse<DriverResponse>> availability(@CurrentUser UserPrincipal principal,
                                                                    @Valid @RequestBody UpdateDriverAvailabilityRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Availability updated",
                driverService.updateAvailability(principal.getId(), request)));
    }
}
