package com.yuvoride.backend.service;

import com.yuvoride.backend.dto.auth.AuthResponse;
import com.yuvoride.backend.dto.auth.TokenResponse;
import com.yuvoride.backend.entity.User;
import com.yuvoride.backend.enums.UserRole;

public interface AuthService {
    void sendOtp(String destination);
    void resendOtp(String destination);
    AuthResponse verifyOtp(String destination, String otp, UserRole role);
    AuthResponse googleLogin(String idToken, UserRole role);
    AuthResponse adminLogin(String username, String password);
    TokenResponse refresh(String refreshToken);
    void logout(Long userId, String refreshToken);

    AuthResponse issueSession(User user, boolean newUser);
}
