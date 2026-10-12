package com.yuvoride.backend.dto.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DriverAuthResponse {
    private final String token;
    private final String refreshToken;
    private final DriverAuthUser user;
}
