package com.yuvoride.backend.dto.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenResponse {
    private final String accessToken;
    private final String token;
    private final String refreshToken;
}
