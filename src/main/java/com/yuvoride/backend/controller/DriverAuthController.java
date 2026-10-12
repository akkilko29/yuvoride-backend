package com.yuvoride.backend.controller;

import com.yuvoride.backend.dto.auth.AuthResponse;
import com.yuvoride.backend.dto.auth.DriverRegisterRequest;
import com.yuvoride.backend.dto.auth.GoogleLoginRequest;
import com.yuvoride.backend.dto.auth.SendOtpRequest;
import com.yuvoride.backend.dto.auth.VerifyOtpRequest;
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
    @Operation(summary = "Send driver OTP. Unregistered emails are allowed; verify-otp will create the driver.")
    public ResponseEntity<ApiResponse<Void>> login(@Valid @RequestBody SendOtpRequest request) {
        driverAuthService.login(request.destination());
        return ResponseEntity.ok(ApiResponse.ok("OTP sent successfully"));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify driver OTP. Creates driver if new. Returns next=HOME|REGISTER|KYC|PENDING_REVIEW|SUSPENDED|REJECTED")
    public ResponseEntity<AuthResponse> verify(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(driverAuthService.verifyOtp(request.destination(), request.getOtp()));
    }

    @PostMapping("/google")
    @Operation(summary = "Driver Google login — same response shape as verify-otp")
    public ResponseEntity<AuthResponse> google(@Valid @RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(driverAuthService.googleLogin(request.getIdToken()));
    }

    @PostMapping("/register")
    @Operation(summary = "Submit or update driver KYC. Same login response so frontend can route with next.")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody DriverRegisterRequest request) {
        return ResponseEntity.ok(driverAuthService.register(request));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Send driver recovery OTP")
    public ResponseEntity<ApiResponse<Void>> forgot(@Valid @RequestBody SendOtpRequest request) {
        driverAuthService.forgotPassword(request.destination());
        return ResponseEntity.ok(ApiResponse.ok("OTP sent successfully"));
    }
}
