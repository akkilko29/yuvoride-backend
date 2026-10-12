package com.yuvoride.backend.dto.auth;

import com.yuvoride.backend.dto.user.UserResponse;
import com.yuvoride.backend.enums.AuthNext;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {
    private final String token;
    private final String accessToken;
    private final String refreshToken;
    private final UserResponse user;
    private final boolean newUser;
    private final boolean registered;
    private final boolean goToHome;
    private final AuthNext next;
    private final String kycStatus;
    private final String accountStatus;
    private final boolean profileComplete;
    private final String message;
}
