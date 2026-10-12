package com.yuvoride.backend.dto.auth;

import com.yuvoride.backend.dto.user.UserResponse;
import com.yuvoride.backend.enums.AuthNextStep;
import com.yuvoride.backend.enums.DriverAccountStatus;
import com.yuvoride.backend.enums.KycStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
    private final String token;
    private final String accessToken;
    private final String refreshToken;
    private final UserResponse user;
    private final boolean registered;
    private final boolean profileComplete;
    private final boolean kycComplete;
    private final boolean canAccessHome;
    private final KycStatus kycStatus;
    private final DriverAccountStatus accountStatus;
    private final AuthNextStep nextStep;
}
