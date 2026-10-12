package com.yuvoride.backend.service;

import com.yuvoride.backend.dto.auth.AuthResponse;
import com.yuvoride.backend.dto.auth.TokenResponse;

public interface AuthService {
    void sendOtp(String phone);
    void resendOtp(String phone);
    AuthResponse verifyOtp(String phone, String otp);
    AuthResponse googleLogin(String idToken);
    TokenResponse refresh(String refreshToken);
    void logout(Long userId, String refreshToken);
}
