package com.yuvoride.backend.service.impl;

import com.yuvoride.backend.config.AppProperties;
import com.yuvoride.backend.dto.auth.AuthResponse;
import com.yuvoride.backend.dto.auth.TokenResponse;
import com.yuvoride.backend.entity.AdminAccount;
import com.yuvoride.backend.entity.Driver;
import com.yuvoride.backend.entity.RefreshToken;
import com.yuvoride.backend.entity.User;
import com.yuvoride.backend.enums.AuthProvider;
import com.yuvoride.backend.enums.UserRole;
import com.yuvoride.backend.enums.UserStatus;
import com.yuvoride.backend.exception.UnauthorizedException;
import com.yuvoride.backend.mapper.AuthSessionMapper;
import com.yuvoride.backend.repository.AdminAccountRepository;
import com.yuvoride.backend.repository.RefreshTokenRepository;
import com.yuvoride.backend.repository.UserRepository;
import com.yuvoride.backend.security.JwtService;
import com.yuvoride.backend.service.AuthService;
import com.yuvoride.backend.service.GoogleTokenVerifier;
import com.yuvoride.backend.service.OtpService;
import com.yuvoride.backend.service.RoleProfileService;
import com.yuvoride.backend.util.AuthIdentity;
import com.yuvoride.backend.util.HashUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final OtpService otpService;
    private final UserRepository userRepository;
    private final AdminAccountRepository adminAccountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final AuthSessionMapper authSessionMapper;
    private final AppProperties properties;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final RoleProfileService roleProfileService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void sendOtp(String destination) {
        otpService.sendOtp(destination);
    }

    @Override
    public void resendOtp(String destination) {
        otpService.sendOtp(destination);
    }

    @Override
    @Transactional
    public AuthResponse verifyOtp(String destination, String otp, UserRole role) {
        otpService.verifyOrThrow(destination, otp);
        return completeOtpOrCreate(destination, role, AuthProvider.OTP, null);
    }

    @Override
    @Transactional
    public AuthResponse googleLogin(String idToken, UserRole role) {
        GoogleTokenVerifier.GoogleProfile profile = googleTokenVerifier.verify(idToken);
        Optional<User> existing = userRepository.findByGoogleSub(profile.subject())
                .or(() -> userRepository.findByEmail(profile.email()));
        boolean newUser = existing.isEmpty();
        User user = existing.orElseGet(() -> createUser(profile.email(), null, role, AuthProvider.GOOGLE, profile));
        AuthSessionMapper.assertRole(user, role);
        if (user.getGoogleSub() == null) {
            user.setGoogleSub(profile.subject());
        }
        if (!StringUtils.hasText(user.getName()) && StringUtils.hasText(profile.name())) {
            user.setName(profile.name());
        }
        if (!StringUtils.hasText(user.getProfilePhoto()) && StringUtils.hasText(profile.picture())) {
            user.setProfilePhoto(profile.picture());
        }
        if (user.getAuthProvider() == AuthProvider.OTP) {
            user.setAuthProvider(AuthProvider.GOOGLE);
        }
        userRepository.save(user);
        return finishLogin(user, newUser);
    }

    @Override
    @Transactional
    public AuthResponse adminLogin(String username, String password) {
        String key = username.trim();
        AdminAccount admin = adminAccountRepository.findByUsernameIgnoreCase(key)
                .or(() -> userRepository.findByEmail(key.toLowerCase(Locale.ROOT))
                        .flatMap(user -> adminAccountRepository.findByUserId(user.getId())))
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));
        User user = admin.getUser();
        AuthSessionMapper.assertRole(user, UserRole.ADMIN);
        assertActive(user);
        if (!StringUtils.hasText(user.getPasswordHash())
                || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid username or password");
        }
        roleProfileService.ensure(user);
        String access = jwtService.generateAccessToken(user);
        String refresh = persistRefreshToken(user);
        return authSessionMapper.admin(user, access, refresh);
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
    public AuthResponse issueSession(User user, boolean newUser) {
        return finishLogin(user, newUser);
    }

    private AuthResponse completeOtpOrCreate(String destination, UserRole role, AuthProvider provider,
                                             GoogleTokenVerifier.GoogleProfile profile) {
        Optional<User> existing = findByDestination(destination);
        boolean newUser = existing.isEmpty();
        User user = existing.orElseGet(() -> {
            if (AuthIdentity.isEmail(destination)) {
                return createUser(destination, null, role, provider, profile);
            }
            return createUser(null, destination, role, provider, profile);
        });
        AuthSessionMapper.assertRole(user, role);
        if (AuthIdentity.isEmail(destination) && !StringUtils.hasText(user.getEmail())) {
            user.setEmail(destination);
            userRepository.save(user);
        }
        if (!AuthIdentity.isEmail(destination) && !StringUtils.hasText(user.getPhone())) {
            user.setPhone(destination);
            userRepository.save(user);
        }
        return finishLogin(user, newUser);
    }

    private AuthResponse finishLogin(User user, boolean newUser) {
        assertActive(user);
        roleProfileService.ensure(user);
        String access = jwtService.generateAccessToken(user);
        String refresh = persistRefreshToken(user);
        if (user.getRole() == UserRole.DRIVER) {
            Driver driver = roleProfileService.ensureDriver(user);
            return authSessionMapper.driver(user, driver, access, refresh, newUser);
        }
        if (user.getRole() == UserRole.ADMIN) {
            return authSessionMapper.admin(user, access, refresh);
        }
        return authSessionMapper.customer(user, access, refresh, newUser);
    }

    private Optional<User> findByDestination(String destination) {
        if (AuthIdentity.isEmail(destination)) {
            return userRepository.findByEmail(destination);
        }
        return userRepository.findByPhone(destination);
    }

    private User createUser(String email, String phone, UserRole role, AuthProvider provider,
                            GoogleTokenVerifier.GoogleProfile profile) {
        User user = User.builder()
                .email(email)
                .phone(phone)
                .name(profile != null ? profile.name() : null)
                .profilePhoto(profile != null ? profile.picture() : null)
                .googleSub(profile != null ? profile.subject() : null)
                .role(role)
                .status(UserStatus.ACTIVE)
                .authProvider(provider)
                .build();
        return userRepository.save(user);
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

    private void assertActive(User user) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Account is not active");
        }
    }
}
