package com.yuvoride.backend.controller;

import com.yuvoride.backend.dto.common.ApiResponse;
import com.yuvoride.backend.dto.common.PagedResponse;
import com.yuvoride.backend.dto.ride.CancelRideRequest;
import com.yuvoride.backend.dto.ride.CreateRideRequest;
import com.yuvoride.backend.dto.ride.RideEstimateRequest;
import com.yuvoride.backend.dto.ride.RideEstimateResponse;
import com.yuvoride.backend.dto.ride.RideResponse;
import com.yuvoride.backend.enums.RideStatus;
import com.yuvoride.backend.security.CurrentUser;
import com.yuvoride.backend.security.UserPrincipal;
import com.yuvoride.backend.service.RideService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/rides")
@Tag(name = "Rides")
@SecurityRequirement(name = "bearerAuth")
public class RideController {

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    @PostMapping("/estimate")
    @Operation(summary = "Calculate route distance, duration, and fare")
    public ResponseEntity<ApiResponse<RideEstimateResponse>> estimate(@Valid @RequestBody RideEstimateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Ride estimate calculated", rideService.estimate(request)));
    }

    @PostMapping
    @Operation(summary = "Create a ride")
    public ResponseEntity<ApiResponse<RideResponse>> create(@CurrentUser UserPrincipal principal,
                                                            @Valid @RequestBody CreateRideRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Ride created", rideService.create(principal, request)));
    }

    @GetMapping("/history")
    @Operation(summary = "Paginated ride history")
    public ResponseEntity<ApiResponse<PagedResponse<RideResponse>>> history(
            @CurrentUser UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) RideStatus status,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        int safeSize = Math.min(Math.max(size, 1), 50);
        Pageable pageable = PageRequest.of(Math.max(page, 0), safeSize, parseSort(sort));
        return ResponseEntity.ok(ApiResponse.ok("Ride history loaded", rideService.history(principal, status, pageable)));
    }

    @GetMapping("/{rideId}")
    @Operation(summary = "Get ride details")
    public ResponseEntity<ApiResponse<RideResponse>> get(@CurrentUser UserPrincipal principal,
                                                         @PathVariable Long rideId) {
        return ResponseEntity.ok(ApiResponse.ok("Ride loaded", rideService.getById(principal, rideId)));
    }

    @PostMapping("/{rideId}/cancel")
    @Operation(summary = "Cancel a ride")
    public ResponseEntity<ApiResponse<RideResponse>> cancel(@CurrentUser UserPrincipal principal,
                                                            @PathVariable Long rideId,
                                                            @Valid @RequestBody CancelRideRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Ride cancelled", rideService.cancel(principal, rideId, request.getReason())));
    }

    @PostMapping("/{rideId}/enroute")
    @Operation(summary = "Driver is arriving")
    public ResponseEntity<ApiResponse<RideResponse>> enroute(@CurrentUser UserPrincipal principal,
                                                             @PathVariable Long rideId) {
        return ResponseEntity.ok(ApiResponse.ok("Driver arriving", rideService.driverEnRoute(principal, rideId)));
    }

    @PostMapping("/{rideId}/arrive")
    @Operation(summary = "Driver has arrived")
    public ResponseEntity<ApiResponse<RideResponse>> arrive(@CurrentUser UserPrincipal principal,
                                                            @PathVariable Long rideId) {
        return ResponseEntity.ok(ApiResponse.ok("Driver arrived", rideService.driverArrive(principal, rideId)));
    }

    @PostMapping("/{rideId}/start")
    @Operation(summary = "Start the ride")
    public ResponseEntity<ApiResponse<RideResponse>> start(@CurrentUser UserPrincipal principal,
                                                           @PathVariable Long rideId) {
        return ResponseEntity.ok(ApiResponse.ok("Ride started", rideService.driverStart(principal, rideId)));
    }

    @PostMapping("/{rideId}/complete")
    @Operation(summary = "Complete the ride")
    public ResponseEntity<ApiResponse<RideResponse>> complete(@CurrentUser UserPrincipal principal,
                                                              @PathVariable Long rideId) {
        return ResponseEntity.ok(ApiResponse.ok("Ride completed", rideService.driverComplete(principal, rideId)));
    }

    private Sort parseSort(String sort) {
        String[] parts = sort.split(",");
        String property = parts[0];
        if (!property.equals("createdAt") && !property.equals("id") && !property.equals("status")) {
            property = "createdAt";
        }
        Sort.Direction direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return Sort.by(direction, property);
    }
}
