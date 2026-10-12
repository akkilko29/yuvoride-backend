package com.yuvoride.backend.controller;

import com.yuvoride.backend.dto.common.ApiResponse;
import com.yuvoride.backend.dto.common.PagedResponse;
import com.yuvoride.backend.dto.driver.DriverLocationRequest;
import com.yuvoride.backend.dto.driver.DriverResponse;
import com.yuvoride.backend.dto.driver.EarningsResponse;
import com.yuvoride.backend.dto.driver.KycResponse;
import com.yuvoride.backend.dto.driver.PatchDriverRequest;
import com.yuvoride.backend.dto.driver.RideStatusUpdateRequest;
import com.yuvoride.backend.dto.driver.UpdateDriverAvailabilityRequest;
import com.yuvoride.backend.dto.driver.UpdateKycRequest;
import com.yuvoride.backend.dto.ride.CancelRideRequest;
import com.yuvoride.backend.dto.ride.RideResponse;
import com.yuvoride.backend.security.CurrentUser;
import com.yuvoride.backend.security.UserPrincipal;
import com.yuvoride.backend.service.DriverRideService;
import com.yuvoride.backend.service.DriverService;
import com.yuvoride.backend.service.EarningsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/driver")
@Tag(name = "Driver App")
@SecurityRequirement(name = "bearerAuth")
public class DriverAppController {

    private final DriverService driverService;
    private final DriverRideService driverRideService;
    private final EarningsService earningsService;

    public DriverAppController(DriverService driverService,
                               DriverRideService driverRideService,
                               EarningsService earningsService) {
        this.driverService = driverService;
        this.driverRideService = driverRideService;
        this.earningsService = earningsService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get driver profile")
    public ResponseEntity<DriverResponse> me(@CurrentUser UserPrincipal principal) {
        return ResponseEntity.ok(driverService.me(principal.getId()));
    }

    @PatchMapping("/me")
    @Operation(summary = "Update driver profile")
    public ResponseEntity<DriverResponse> patch(@CurrentUser UserPrincipal principal,
                                                @Valid @RequestBody PatchDriverRequest request) {
        return ResponseEntity.ok(driverService.updateProfile(principal.getId(), request));
    }

    @PostMapping("/availability")
    @Operation(summary = "Go online or offline")
    public ResponseEntity<DriverResponse> availability(@CurrentUser UserPrincipal principal,
                                                       @Valid @RequestBody UpdateDriverAvailabilityRequest request) {
        return ResponseEntity.ok(driverService.updateAvailability(principal.getId(), request));
    }

    @PostMapping("/location")
    @Operation(summary = "Update driver GPS location")
    public ResponseEntity<DriverResponse> location(@CurrentUser UserPrincipal principal,
                                                   @Valid @RequestBody DriverLocationRequest request) {
        return ResponseEntity.ok(driverService.updateLocation(principal.getId(), request));
    }

    @GetMapping("/kyc")
    @Operation(summary = "Get driver KYC submission")
    public ResponseEntity<KycResponse> getKyc(@CurrentUser UserPrincipal principal) {
        return ResponseEntity.ok(driverService.getKyc(principal.getId()));
    }

    @PatchMapping("/kyc")
    @Operation(summary = "Update / resubmit driver KYC")
    public ResponseEntity<KycResponse> patchKyc(@CurrentUser UserPrincipal principal,
                                                @RequestBody UpdateKycRequest request) {
        return ResponseEntity.ok(driverService.updateKyc(principal.getId(), request));
    }

    @PutMapping("/kyc")
    @Operation(summary = "Update / resubmit driver KYC")
    public ResponseEntity<KycResponse> putKyc(@CurrentUser UserPrincipal principal,
                                              @RequestBody UpdateKycRequest request) {
        return ResponseEntity.ok(driverService.updateKyc(principal.getId(), request));
    }

    @GetMapping("/rides/requests")
    public ResponseEntity<List<RideResponse>> requests(@CurrentUser UserPrincipal principal) {
        return ResponseEntity.ok(driverRideService.openRequests(principal));
    }

    @GetMapping("/rides/active")
    public ResponseEntity<RideResponse> active(@CurrentUser UserPrincipal principal) {
        return ResponseEntity.ok(driverRideService.active(principal));
    }

    @GetMapping("/rides/history")
    public ResponseEntity<PagedResponse<RideResponse>> history(@CurrentUser UserPrincipal principal,
                                                               @RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(driverRideService.history(principal, PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50))));
    }

    @GetMapping("/rides/{id}")
    public ResponseEntity<RideResponse> get(@CurrentUser UserPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(driverRideService.get(principal, id));
    }

    @PostMapping("/rides/{id}/accept")
    public ResponseEntity<RideResponse> accept(@CurrentUser UserPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(driverRideService.accept(principal, id));
    }

    @PostMapping("/rides/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> reject(@CurrentUser UserPrincipal principal, @PathVariable Long id) {
        driverRideService.reject(principal, id);
        return ResponseEntity.ok(ApiResponse.ok("Ride rejected"));
    }

    @PostMapping("/rides/{id}/start")
    public ResponseEntity<RideResponse> start(@CurrentUser UserPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(driverRideService.start(principal, id));
    }

    @PostMapping("/rides/{id}/complete")
    public ResponseEntity<RideResponse> complete(@CurrentUser UserPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(driverRideService.complete(principal, id));
    }

    @PostMapping("/rides/{id}/status")
    public ResponseEntity<RideResponse> status(@CurrentUser UserPrincipal principal,
                                               @PathVariable Long id,
                                               @Valid @RequestBody RideStatusUpdateRequest request) {
        return ResponseEntity.ok(driverRideService.updateStatus(principal, id, request.getStatus()));
    }

    @PostMapping("/rides/{id}/cancel")
    public ResponseEntity<RideResponse> cancel(@CurrentUser UserPrincipal principal,
                                               @PathVariable Long id,
                                               @Valid @RequestBody CancelRideRequest request) {
        return ResponseEntity.ok(driverRideService.cancel(principal, id, request.getReason()));
    }

    @GetMapping("/earnings")
    public ResponseEntity<EarningsResponse> earnings(@CurrentUser UserPrincipal principal) {
        return ResponseEntity.ok(earningsService.forDriver(principal.getId()));
    }
}
