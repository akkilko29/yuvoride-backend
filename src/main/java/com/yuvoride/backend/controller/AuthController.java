package com.yuvoride.backend.controller;

import com.yuvoride.backend.dto.auth.AuthResponse;
import com.yuvoride.backend.dto.auth.GoogleLoginRequest;
import com.yuvoride.backend.dto.auth.LogoutRequest;
import com.yuvoride.backend.dto.auth.RefreshTokenRequest;
import com.yuvoride.backend.dto.auth.SendOtpRequest;
import com.yuvoride.backend.dto.auth.TokenResponse;
import com.yuvoride.backend.dto.auth.VerifyOtpRequest;
import com.yuvoride.backend.dto.common.ApiResponse;
import com.yuvoride.backend.enums.UserRole;
import com.yuvoride.backend.security.CurrentUser;
import com.yuvoride.backend.security.UserPrincipal;
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
@RequestMapping("/v1/auth")
@Tag(name = "Auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/otp/send")
    @Operation(summary = "Send OTP to phone or email")
    public ResponseEntity<ApiResponse<Void>> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        authService.sendOtp(request.destination());
        return ResponseEntity.ok(ApiResponse.ok("OTP sent successfully"));
    }

    @PostMapping("/otp/verify")
    @Operation(summary = "Verify OTP and login as customer")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Login successful",
                authService.verifyOtp(request.destination(), request.getOtp(), UserRole.CUSTOMER)));
    }

    @PostMapping("/otp/resend")
    @Operation(summary = "Resend OTP")
    public ResponseEntity<ApiResponse<Void>> resendOtp(@Valid @RequestBody SendOtpRequest request) {
        authService.resendOtp(request.destination());
        return ResponseEntity.ok(ApiResponse.ok("OTP sent successfully"));
    }

    @PostMapping("/google")
    @Operation(summary = "Customer Google login")
    public ResponseEntity<ApiResponse<AuthResponse>> google(@Valid @RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Login successful",
                authService.googleLogin(request.getIdToken(), UserRole.CUSTOMER)));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotate refresh token")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Token refreshed", authService.refresh(request.getRefreshToken())));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke refresh tokens")
    public ResponseEntity<ApiResponse<Void>> logout(@CurrentUser UserPrincipal principal,
                                                    @RequestBody(required = false) LogoutRequest request) {
        Long userId = principal != null ? principal.getId() : null;
        String refreshToken = request != null ? request.getRefreshToken() : null;
        authService.logout(userId, refreshToken);
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully"));
    }
}
