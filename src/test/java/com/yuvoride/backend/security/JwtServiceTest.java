package com.yuvoride.backend.security;

import com.yuvoride.backend.config.AppProperties;
import com.yuvoride.backend.entity.User;
import com.yuvoride.backend.enums.AuthProvider;
import com.yuvoride.backend.enums.UserRole;
import com.yuvoride.backend.enums.UserStatus;
import com.yuvoride.backend.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        AppProperties properties = new AppProperties();
        properties.getJwt().setSecret("test-secret-key-that-is-long-enough-for-hs256-algorithms-ok");
        properties.getJwt().setAccessTokenExpirationMs(60_000);
        jwtService = new JwtService(properties);
    }

    @Test
    void roundTripsUserId() {
        User user = User.builder()
                .id(42L)
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .authProvider(AuthProvider.OTP)
                .build();

        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.parseUserId(token)).isEqualTo(42L);
    }

    @Test
    void rejectsTamperedToken() {
        User user = User.builder()
                .id(42L)
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .authProvider(AuthProvider.OTP)
                .build();
        String token = jwtService.generateAccessToken(user);
        String tampered = token.substring(0, token.length() - 8) + "AAAAAAAA";

        assertThatThrownBy(() -> jwtService.parseUserId(tampered))
                .isInstanceOf(UnauthorizedException.class);
        assertThatThrownBy(() -> jwtService.parseUserId("not-a-valid-jwt"))
                .isInstanceOf(UnauthorizedException.class);
    }
}
