package com.yuvoride.backend.dto.auth;

import com.yuvoride.backend.dto.user.UserResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {
    private final String accessToken;
    private final String refreshToken;
    private final UserResponse user;
}
