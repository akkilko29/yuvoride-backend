package com.yuvoride.backend.service.impl;

import com.yuvoride.backend.config.AppProperties;
import com.yuvoride.backend.dto.auth.AuthResponse;
import com.yuvoride.backend.dto.auth.TokenResponse;
import com.yuvoride.backend.entity.RefreshToken;
import com.yuvoride.backend.entity.User;
import com.yuvoride.backend.enums.AuthProvider;
import com.yuvoride.backend.enums.UserRole;
import com.yuvoride.backend.enums.UserStatus;
import com.yuvoride.backend.exception.UnauthorizedException;
import com.yuvoride.backend.mapper.UserMapper;
import com.yuvoride.backend.repository.RefreshTokenRepository;
import com.yuvoride.backend.repository.UserRepository;
import com.yuvoride.backend.security.JwtService;
import com.yuvoride.backend.service.AuthService;
import com.yuvoride.backend.service.GoogleTokenVerifier;
import com.yuvoride.backend.service.OtpService;
import com.yuvoride.backend.service.RoleProfileService;
import com.yuvoride.backend.util.HashUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final OtpService otpService;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final AppProperties properties;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final RoleProfileService roleProfileService;

    @Override
    public void sendOtp(String phone) {
        otpService.sendOtp(phone);
    }

    @Override
    public void resendOtp(String phone) {
        otpService.sendOtp(phone);
    }

    @Override
    @Transactional
    public AuthResponse verifyOtp(String phone, String otp) {
        otpService.verifyOrThrow(phone, otp);
        User user = userRepository.findByPhone(phone).orElseGet(() -> createOtpUser(phone));
        assertActive(user);
        roleProfileService.ensure(user);
        return issueAuth(user);
    }

    @Override
    @Transactional
    public AuthResponse googleLogin(String idToken) {
        GoogleTokenVerifier.GoogleProfile profile = googleTokenVerifier.verify(idToken);
        User user = userRepository.findByGoogleSub(profile.subject())
                .or(() -> userRepository.findByEmail(profile.email()))
                .orElseGet(() -> createGoogleUser(profile));
        if (user.getGoogleSub() == null) {
            user.setGoogleSub(profile.subject());
        }
        if (!StringUtils.hasText(user.getName()) && StringUtils.hasText(profile.name())) {
            user.setName(profile.name());
        }
        if (!StringUtils.hasText(user.getProfilePhoto()) && StringUtils.hasText(profile.picture())) {
            user.setProfilePhoto(profile.picture());
        }
        userRepository.save(user);
        assertActive(user);
        roleProfileService.ensure(user);
        return issueAuth(user);
    }

    @Override
    @Transactional
    public TokenResponse refresh(String refreshToken) {
        String hash = HashUtils.sha256(refreshToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHashAndRevokedFalse(hash)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        if (stored.getExpiresAt().isBefore(Instant.now())) {
            stored.setRevoked(true);
            stored.setRevokedAt(Instant.now());
            refreshTokenRepository.save(stored);
            throw new UnauthorizedException("Refresh token has expired");
        }
        stored.setRevoked(true);
        stored.setRevokedAt(Instant.now());
        refreshTokenRepository.save(stored);

        User user = stored.getUser();
        assertActive(user);
        String access = jwtService.generateAccessToken(user);
        String nextRefresh = persistRefreshToken(user);
        return TokenResponse.builder()
                .accessToken(access)
                .token(access)
                .refreshToken(nextRefresh)
                .build();
    }

    @Override
    @Transactional
    public void logout(Long userId, String refreshToken) {
        Instant now = Instant.now();
        if (userId != null) {
            refreshTokenRepository.revokeAllForUser(userId, now);
            return;
        }
        if (!StringUtils.hasText(refreshToken)) {
            throw new UnauthorizedException("Refresh token is required to logout");
        }
        refreshTokenRepository.findByTokenHashAndRevokedFalse(HashUtils.sha256(refreshToken))
                .ifPresent(token -> {
                    token.setRevoked(true);
                    token.setRevokedAt(now);
                    refreshTokenRepository.save(token);
                });
    }

    @Override
    @Transactional
    public AuthResponse issueSession(User user) {
        return issueAuth(user);
    }

    private AuthResponse issueAuth(User user) {
        return AuthResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(persistRefreshToken(user))
                .user(userMapper.toResponse(user))
                .build();
    }

    private String persistRefreshToken(User user) {
        String raw = UUID.randomUUID().toString();
        Instant now = Instant.now();
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .tokenHash(HashUtils.sha256(raw))
                .expiresAt(now.plusMillis(properties.getJwt().getRefreshTokenExpirationMs()))
                .createdAt(now)
                .revoked(false)
                .build();
        refreshTokenRepository.save(token);
        return raw;
    }

    private User createOtpUser(String phone) {
        User user = User.builder()
                .phone(phone)
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .authProvider(AuthProvider.OTP)
                .build();
        return userRepository.save(user);
    }

    private User createGoogleUser(GoogleTokenVerifier.GoogleProfile profile) {
        User user = User.builder()
                .email(profile.email())
                .name(profile.name())
                .profilePhoto(profile.picture())
                .googleSub(profile.subject())
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .authProvider(AuthProvider.GOOGLE)
                .build();
        return userRepository.save(user);
    }

    private void assertActive(User user) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Account is not active");
        }
    }
}
