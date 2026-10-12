package com.yuvoride.backend.service;

import com.yuvoride.backend.dto.auth.DriverAuthResponse;
import com.yuvoride.backend.dto.auth.DriverRegisterRequest;

public interface DriverAuthService {
    void login(String email);
    DriverAuthResponse verifyOtp(String email, String otp);
    void register(DriverRegisterRequest request);
    void forgotPassword(String email);
}
