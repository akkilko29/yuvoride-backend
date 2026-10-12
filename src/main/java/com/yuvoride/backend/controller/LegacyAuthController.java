package com.yuvoride.backend.controller;

import com.yuvoride.backend.dto.auth.LogoutRequest;
import com.yuvoride.backend.dto.auth.RefreshTokenRequest;
import com.yuvoride.backend.dto.auth.TokenResponse;
import com.yuvoride.backend.dto.common.ApiResponse;
import com.yuvoride.backend.security.CurrentUser;
import com.yuvoride.backend.security.UserPrincipal;
import com.yuvoride.backend.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Legacy Auth")
public class LegacyAuthController {

    private final AuthService authService;

    public LegacyAuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@CurrentUser UserPrincipal principal,
                                                    @RequestBody(required = false) LogoutRequest request) {
        Long userId = principal != null ? principal.getId() : null;
        String refreshToken = request != null ? request.getRefreshToken() : null;
        authService.logout(userId, refreshToken);
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully"));
    }
}
