package com.yuvoride.backend.dto.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DriverAuthUser {
    private final String id;
    private final String email;
    private final String role;
    private final boolean profileComplete;
    private final String accountStatus;
    private final String kycStatus;
}
