package com.yuvoride.backend.mapper;

import com.yuvoride.backend.dto.auth.AuthResponse;
import com.yuvoride.backend.entity.Driver;
import com.yuvoride.backend.entity.User;
import com.yuvoride.backend.enums.AuthNext;
import com.yuvoride.backend.enums.DriverAccountStatus;
import com.yuvoride.backend.enums.KycStatus;
import com.yuvoride.backend.enums.UserRole;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AuthSessionMapper {

    private final UserMapper userMapper;

    public AuthSessionMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public AuthResponse customer(User user, String accessToken, String refreshToken, boolean newUser) {
        return base(user, accessToken, refreshToken, newUser, null, true, true, AuthNext.HOME,
                "Login successful");
    }

    public AuthResponse admin(User user, String accessToken, String refreshToken) {
        return base(user, accessToken, refreshToken, false, null, true, true, AuthNext.HOME,
                "Login successful");
    }

    public AuthResponse driver(User user, Driver driver, String accessToken, String refreshToken, boolean newUser) {
        boolean profileComplete = isProfileComplete(driver);
        AuthNext next = resolveDriverNext(driver, profileComplete);
        boolean goToHome = next == AuthNext.HOME;
        boolean registered = driver != null && profileComplete;
        return base(user, accessToken, refreshToken, newUser, driver, registered, profileComplete, next,
                message(next));
    }

    private AuthResponse base(User user, String accessToken, String refreshToken, boolean newUser,
                              Driver driver, boolean registered, boolean profileComplete,
                              AuthNext next, String message) {
        return AuthResponse.builder()
                .token(accessToken)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userMapper.toResponse(user))
                .newUser(newUser)
                .registered(registered)
                .goToHome(next == AuthNext.HOME)
                .next(next)
                .kycStatus(driver != null && driver.getKycStatus() != null ? driver.getKycStatus().name() : null)
                .accountStatus(driver != null && driver.getAccountStatus() != null ? driver.getAccountStatus().name() : null)
                .profileComplete(profileComplete)
                .message(message)
                .build();
    }

    public static boolean isProfileComplete(Driver driver) {
        return driver != null
                && StringUtils.hasText(driver.getLicenseNumber())
                && StringUtils.hasText(driver.getVehicleNumber());
    }

    public static AuthNext resolveDriverNext(Driver driver, boolean profileComplete) {
        if (driver == null) {
            return AuthNext.REGISTER;
        }
        if (driver.getAccountStatus() == DriverAccountStatus.SUSPENDED) {
            return AuthNext.SUSPENDED;
        }
        if (driver.getAccountStatus() == DriverAccountStatus.REJECTED
                || driver.getKycStatus() == KycStatus.REJECTED) {
            return AuthNext.REJECTED;
        }
        if (!profileComplete) {
            return AuthNext.REGISTER;
        }
        if (driver.getKycStatus() == KycStatus.PENDING) {
            return AuthNext.PENDING_REVIEW;
        }
        if (driver.getKycStatus() == KycStatus.VERIFIED
                && driver.getAccountStatus() == DriverAccountStatus.ACTIVE) {
            return AuthNext.HOME;
        }
        return AuthNext.KYC;
    }

    private static String message(AuthNext next) {
        return switch (next) {
            case HOME -> "Login successful";
            case REGISTER -> "Complete driver registration and KYC";
            case KYC -> "Update your KYC documents";
            case PENDING_REVIEW -> "KYC is under review";
            case SUSPENDED -> "Driver account is suspended";
            case REJECTED -> "KYC was rejected. Please resubmit documents";
        };
    }

    public static void assertRole(User user, UserRole expected) {
        if (user.getRole() != expected) {
            throw new com.yuvoride.backend.exception.ConflictException(
                    com.yuvoride.backend.exception.ErrorCodes.CONFLICT,
                    "This account is registered as " + user.getRole() + ". Use the " + user.getRole().name().toLowerCase() + " app.");
        }
    }
}
