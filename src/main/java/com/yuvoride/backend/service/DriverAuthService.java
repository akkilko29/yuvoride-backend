package com.yuvoride.backend.service;

import com.yuvoride.backend.dto.auth.AuthResponse;
import com.yuvoride.backend.dto.auth.DriverRegisterRequest;

public interface DriverAuthService {
    void login(String destination);
    AuthResponse verifyOtp(String destination, String otp);
    AuthResponse googleLogin(String idToken);
    AuthResponse register(DriverRegisterRequest request);
    void forgotPassword(String destination);
}
