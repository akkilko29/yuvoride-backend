package com.yuvoride.backend.controller;

import com.yuvoride.backend.dto.auth.DriverAuthResponse;
import com.yuvoride.backend.dto.auth.DriverLoginRequest;
import com.yuvoride.backend.dto.auth.DriverRegisterRequest;
import com.yuvoride.backend.dto.auth.DriverVerifyOtpRequest;
import com.yuvoride.backend.dto.common.ApiResponse;
import com.yuvoride.backend.service.DriverAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/driver")
@Tag(name = "Driver Auth")
public class DriverAuthController {

    private final DriverAuthService driverAuthService;

    public DriverAuthController(DriverAuthService driverAuthService) {
        this.driverAuthService = driverAuthService;
    }

    @PostMapping("/login")
    @Operation(summary = "Send driver login OTP to email")
    public ResponseEntity<ApiResponse<Void>> login(@Valid @RequestBody DriverLoginRequest request) {
        driverAuthService.login(request.getEmail());
        return ResponseEntity.ok(ApiResponse.ok("OTP sent successfully"));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify driver OTP")
    public ResponseEntity<DriverAuthResponse> verify(@Valid @RequestBody DriverVerifyOtpRequest request) {
        return ResponseEntity.ok(driverAuthService.verifyOtp(request.getEmail(), request.getOtp()));
    }

    @PostMapping("/register")
    @Operation(summary = "Register driver KYC profile")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody DriverRegisterRequest request) {
        driverAuthService.register(request);
        return ResponseEntity.ok(ApiResponse.ok("Registration submitted for KYC review"));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Send driver recovery OTP")
    public ResponseEntity<ApiResponse<Void>> forgot(@Valid @RequestBody DriverLoginRequest request) {
        driverAuthService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(ApiResponse.ok("OTP sent successfully"));
    }
}
