package com.yuvoride.backend.service.impl;

import com.yuvoride.backend.dto.auth.AuthResponse;
import com.yuvoride.backend.dto.auth.DriverAuthResponse;
import com.yuvoride.backend.dto.auth.DriverAuthUser;
import com.yuvoride.backend.dto.auth.DriverRegisterRequest;
import com.yuvoride.backend.entity.Driver;
import com.yuvoride.backend.entity.User;
import com.yuvoride.backend.entity.VehicleType;
import com.yuvoride.backend.enums.AuthProvider;
import com.yuvoride.backend.enums.DriverAccountStatus;
import com.yuvoride.backend.enums.DriverStatus;
import com.yuvoride.backend.enums.IdentityType;
import com.yuvoride.backend.enums.KycStatus;
import com.yuvoride.backend.enums.UserRole;
import com.yuvoride.backend.enums.UserStatus;
import com.yuvoride.backend.exception.BadRequestException;
import com.yuvoride.backend.exception.ConflictException;
import com.yuvoride.backend.exception.ErrorCodes;
import com.yuvoride.backend.exception.ResourceNotFoundException;
import com.yuvoride.backend.repository.DriverRepository;
import com.yuvoride.backend.repository.UserRepository;
import com.yuvoride.backend.repository.VehicleTypeRepository;
import com.yuvoride.backend.service.AuthService;
import com.yuvoride.backend.service.DriverAuthService;
import com.yuvoride.backend.service.FileStorageService;
import com.yuvoride.backend.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverAuthServiceImpl implements DriverAuthService {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final VehicleTypeRepository vehicleTypeRepository;
    private final OtpService otpService;
    private final AuthService authService;
    private final FileStorageService fileStorageService;

    @Override
    public void login(String email) {
        User user = userRepository.findByEmail(email.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ResourceNotFoundException("Driver account not found. Register first."));
        if (user.getRole() != UserRole.DRIVER) {
            throw new BadRequestException("This email is not registered as a driver");
        }
        otpService.sendOtp(email.toLowerCase(Locale.ROOT));
        log.info("Driver login OTP sent");
    }

    @Override
    @Transactional
    public DriverAuthResponse verifyOtp(String email, String otp) {
        String key = email.toLowerCase(Locale.ROOT);
        otpService.verifyOrThrow(key, otp);
        User user = userRepository.findByEmail(key)
                .orElseThrow(() -> new ResourceNotFoundException("Driver account not found"));
        if (user.getRole() != UserRole.DRIVER) {
            throw new BadRequestException("This email is not registered as a driver");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new com.yuvoride.backend.exception.UnauthorizedException("Account is not active");
        }
        AuthResponse session = authService.issueSession(user);
        Driver driver = driverRepository.findByUserId(user.getId()).orElse(null);
        boolean profileComplete = driver != null
                && StringUtils.hasText(driver.getLicenseNumber())
                && StringUtils.hasText(driver.getVehicleNumber());
        return DriverAuthResponse.builder()
                .token(session.getAccessToken())
                .refreshToken(session.getRefreshToken())
                .user(DriverAuthUser.builder()
                        .id(String.valueOf(user.getId()))
                        .email(user.getEmail())
                        .role(UserRole.DRIVER.name())
                        .profileComplete(profileComplete)
                        .accountStatus(driver != null ? driver.getAccountStatus().name() : DriverAccountStatus.INCOMPLETE.name())
                        .kycStatus(driver != null ? driver.getKycStatus().name() : KycStatus.PENDING.name())
                        .build())
                .build();
    }

    @Override
    @Transactional
    public void register(DriverRegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        String phone = request.getPhone().trim();
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException(ErrorCodes.CONFLICT, "Email is already registered");
        }
        if (userRepository.existsByPhone(phone)) {
            throw new ConflictException(ErrorCodes.CONFLICT, "Phone is already registered");
        }
        User user = userRepository.save(User.builder()
                .name(request.getName().trim())
                .email(email)
                .phone(phone)
                .role(UserRole.DRIVER)
                .status(UserStatus.ACTIVE)
                .authProvider(AuthProvider.OTP)
                .profilePhoto(fileStorageService.storeReference(request.getPhotoUri()))
                .build());

        VehicleType vehicleType = null;
        if (StringUtils.hasText(request.getVehicleType())) {
            vehicleType = vehicleTypeRepository.findByNameIgnoreCase(request.getVehicleType().trim()).orElse(null);
        }

        Driver driver = Driver.builder()
                .user(user)
                .dob(parseDob(request.getDob()))
                .address(request.getAddress())
                .licenseNumber(blankToNull(request.getLicenseNumber()))
                .vehicleNumber(blankToNull(request.getVehicleNumber()))
                .rcNumber(blankToNull(request.getRcNumber()))
                .identityType(parseIdentity(request.getIdentityType()))
                .identityNumber(blankToNull(request.getIdentityNumber()))
                .photoRef(fileStorageService.storeReference(request.getPhotoUri()))
                .identityRef(fileStorageService.storeReference(request.getIdentityUri()))
                .rcRef(fileStorageService.storeReference(request.getRcUri()))
                .licenseRef(fileStorageService.storeReference(request.getLicenseUri()))
                .preferredVehicleType(vehicleType)
                .status(DriverStatus.OFFLINE)
                .accountStatus(DriverAccountStatus.PENDING)
                .kycStatus(KycStatus.PENDING)
                .online(false)
                .available(false)
                .rating(new BigDecimal("5.00"))
                .totalRides(0)
                .build();
        driverRepository.save(driver);
        log.info("Driver registered userId={}", user.getId());
    }

    @Override
    public void forgotPassword(String email) {
        login(email);
    }

    private LocalDate parseDob(String dob) {
        if (!StringUtils.hasText(dob)) {
            return null;
        }
        try {
            return LocalDate.parse(dob);
        } catch (Exception ex) {
            throw new BadRequestException("dob must be ISO-8601 (yyyy-MM-dd)");
        }
    }

    private IdentityType parseIdentity(String value) {
        if (!StringUtils.hasText(value)) {
            return IdentityType.OTHER;
        }
        try {
            return IdentityType.valueOf(value.trim().toUpperCase(Locale.ROOT).replace(' ', '_'));
        } catch (IllegalArgumentException ex) {
            return IdentityType.OTHER;
        }
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
