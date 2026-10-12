package com.yuvoride.backend.controller;

import com.yuvoride.backend.dto.auth.AuthResponse;
import com.yuvoride.backend.dto.auth.GoogleLoginRequest;
import com.yuvoride.backend.dto.auth.SendOtpRequest;
import com.yuvoride.backend.dto.auth.VerifyOtpRequest;
import com.yuvoride.backend.dto.common.ApiResponse;
import com.yuvoride.backend.enums.UserRole;
import com.yuvoride.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/customer")
@Tag(name = "Customer Auth")
public class CustomerAuthController {

    private final AuthService authService;

    public CustomerAuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Send customer OTP to email or phone")
    public ResponseEntity<ApiResponse<Void>> login(@Valid @RequestBody SendOtpRequest request) {
        authService.sendOtp(request.destination());
        return ResponseEntity.ok(ApiResponse.ok("OTP sent successfully"));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify customer OTP. If new, register then goToHome=true")
    public ResponseEntity<AuthResponse> verify(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(authService.verifyOtp(request.destination(), request.getOtp(), UserRole.CUSTOMER));
    }

    @PostMapping("/google")
    @Operation(summary = "Customer Google login")
    public ResponseEntity<AuthResponse> google(@Valid @RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(authService.googleLogin(request.getIdToken(), UserRole.CUSTOMER));
    }
}
