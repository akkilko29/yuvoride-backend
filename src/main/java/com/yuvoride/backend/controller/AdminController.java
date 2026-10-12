package com.yuvoride.backend.controller;

import com.yuvoride.backend.dto.admin.AdminDriverStatusRequest;
import com.yuvoride.backend.dto.admin.AdminKycRequest;
import com.yuvoride.backend.dto.common.ApiResponse;
import com.yuvoride.backend.dto.driver.DriverResponse;
import com.yuvoride.backend.dto.ride.RideResponse;
import com.yuvoride.backend.dto.user.UserResponse;
import com.yuvoride.backend.service.AdminService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/admin")
@Tag(name = "Admin")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/drivers")
    public ResponseEntity<ApiResponse<List<DriverResponse>>> drivers() {
        return ResponseEntity.ok(ApiResponse.ok("Drivers loaded", adminService.listDrivers()));
    }

    @GetMapping("/drivers/{id}")
    public ResponseEntity<ApiResponse<DriverResponse>> driver(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Driver loaded", adminService.getDriver(id)));
    }

    @PatchMapping("/drivers/{id}/kyc")
    public ResponseEntity<ApiResponse<DriverResponse>> kyc(@PathVariable Long id,
                                                           @Valid @RequestBody AdminKycRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("KYC updated", adminService.updateKyc(id, request)));
    }

    @PatchMapping("/drivers/{id}/status")
    public ResponseEntity<ApiResponse<DriverResponse>> status(@PathVariable Long id,
                                                              @Valid @RequestBody AdminDriverStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated", adminService.updateStatus(id, request)));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> users(@RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok("Users loaded",
                adminService.listUsers(PageRequest.of(page, Math.min(size, 50)))));
    }

    @GetMapping("/customers")
    public ResponseEntity<ApiResponse<List<UserResponse>>> customers(@RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok("Customers loaded",
                adminService.listCustomers(PageRequest.of(page, Math.min(size, 50)))));
    }

    @GetMapping("/rides")
    public ResponseEntity<ApiResponse<List<RideResponse>>> rides(@RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok("Rides loaded",
                adminService.listRides(PageRequest.of(page, Math.min(size, 50)))));
    }
}
