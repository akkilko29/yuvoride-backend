package com.yuvoride.backend.controller;

import com.yuvoride.backend.dto.auth.AdminLoginRequest;
import com.yuvoride.backend.dto.auth.AuthResponse;
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
@RequestMapping("/auth/admin")
@Tag(name = "Admin Auth")
public class AdminAuthController {

    private final AuthService authService;

    public AdminAuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Admin username and password login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return ResponseEntity.ok(authService.adminLogin(request.getUsername(), request.getPassword()));
    }
}
