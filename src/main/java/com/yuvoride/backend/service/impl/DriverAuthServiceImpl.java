package com.yuvoride.backend.service.impl;

import com.yuvoride.backend.dto.auth.AuthResponse;
import com.yuvoride.backend.dto.auth.DriverRegisterRequest;
import com.yuvoride.backend.entity.Driver;
import com.yuvoride.backend.entity.DriverKyc;
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
import com.yuvoride.backend.mapper.AuthSessionMapper;
import com.yuvoride.backend.repository.DriverKycRepository;
import com.yuvoride.backend.repository.DriverRepository;
import com.yuvoride.backend.repository.UserRepository;
import com.yuvoride.backend.repository.VehicleTypeRepository;
import com.yuvoride.backend.service.AuthService;
import com.yuvoride.backend.service.DriverAuthService;
import com.yuvoride.backend.service.FileStorageService;
import com.yuvoride.backend.service.OtpService;
import com.yuvoride.backend.service.RoleProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverAuthServiceImpl implements DriverAuthService {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final DriverKycRepository driverKycRepository;
    private final VehicleTypeRepository vehicleTypeRepository;
    private final OtpService otpService;
    private final AuthService authService;
    private final FileStorageService fileStorageService;
    private final RoleProfileService roleProfileService;

    @Override
    public void login(String destination) {
        userRepository.findByEmail(destination).or(() -> userRepository.findByPhone(destination))
                .ifPresent(user -> AuthSessionMapper.assertRole(user, UserRole.DRIVER));
        otpService.sendOtp(destination);
        log.info("Driver login OTP sent");
    }

    @Override
    @Transactional
    public AuthResponse verifyOtp(String destination, String otp) {
        return authService.verifyOtp(destination, otp, UserRole.DRIVER);
    }

    @Override
    @Transactional
    public AuthResponse googleLogin(String idToken) {
        return authService.googleLogin(idToken, UserRole.DRIVER);
    }

    @Override
    @Transactional
    public AuthResponse register(DriverRegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        String phone = request.getPhone().trim();
        User user = userRepository.findByEmail(email).orElse(null);
        boolean newUser = user == null;
        if (user == null) {
            userRepository.findByPhone(phone).ifPresent(existing -> {
                throw new ConflictException(ErrorCodes.CONFLICT, "Phone is already registered");
            });
            user = userRepository.save(User.builder()
                    .name(request.getName().trim())
                    .email(email)
                    .phone(phone)
                    .role(UserRole.DRIVER)
                    .status(UserStatus.ACTIVE)
                    .authProvider(AuthProvider.OTP)
                    .profilePhoto(fileStorageService.storeReference(request.getPhotoUri()))
                    .build());
        } else {
            AuthSessionMapper.assertRole(user, UserRole.DRIVER);
            if (StringUtils.hasText(request.getName())) {
                user.setName(request.getName().trim());
            }
            if (StringUtils.hasText(phone) && !phone.equals(user.getPhone())) {
                Long userId = user.getId();
                userRepository.findByPhone(phone)
                        .filter(other -> !other.getId().equals(userId))
                        .ifPresent(other -> {
                            throw new ConflictException(ErrorCodes.CONFLICT, "Phone is already registered");
                        });
                user.setPhone(phone);
            }
            user.setProfilePhoto(fileStorageService.storeReference(request.getPhotoUri()));
            userRepository.save(user);
        }

        Driver driver = roleProfileService.ensureDriver(user);
        applyRegistration(driver, request);
        driver.setAccountStatus(DriverAccountStatus.PENDING);
        driver.setKycStatus(KycStatus.PENDING);
        driver.setOnline(false);
        driver.setAvailable(false);
        driver.setStatus(DriverStatus.OFFLINE);
        driverRepository.save(driver);

        roleProfileService.ensureDriverKyc(driver);
        driverKycRepository.findByDriverId(driver.getId()).ifPresent(kyc -> {
            copyToKyc(driver, kyc, request);
            kyc.setStatus(KycStatus.PENDING);
            kyc.setRejectionReason(null);
            kyc.setSubmittedAt(Instant.now());
            kyc.setReviewedAt(null);
            driverKycRepository.save(kyc);
        });
        log.info("Driver KYC submitted userId={} driverId={}", user.getId(), driver.getId());
        return authService.issueSession(user, newUser);
    }

    @Override
    public void forgotPassword(String destination) {
        login(destination);
    }

    private void applyRegistration(Driver driver, DriverRegisterRequest request) {
        VehicleType vehicleType = null;
        if (StringUtils.hasText(request.getVehicleType())) {
            vehicleType = vehicleTypeRepository.findByNameIgnoreCase(request.getVehicleType().trim()).orElse(null);
        }
        driver.setDob(parseDob(request.getDob()));
        driver.setAddress(request.getAddress());
        driver.setLicenseNumber(blankToNull(request.getLicenseNumber()));
        driver.setVehicleNumber(blankToNull(request.getVehicleNumber()));
        driver.setRcNumber(blankToNull(request.getRcNumber()));
        driver.setIdentityType(parseIdentity(request.getIdentityType()));
        driver.setIdentityNumber(blankToNull(request.getIdentityNumber()));
        driver.setPhotoRef(fileStorageService.storeReference(request.getPhotoUri()));
        driver.setIdentityRef(fileStorageService.storeReference(request.getIdentityUri()));
        driver.setRcRef(fileStorageService.storeReference(request.getRcUri()));
        driver.setLicenseRef(fileStorageService.storeReference(request.getLicenseUri()));
        driver.setPreferredVehicleType(vehicleType);
    }

    private void copyToKyc(Driver driver, DriverKyc kyc, DriverRegisterRequest request) {
        kyc.setDob(driver.getDob());
        kyc.setAddress(driver.getAddress());
        kyc.setIdentityType(driver.getIdentityType());
        kyc.setIdentityNumber(driver.getIdentityNumber());
        kyc.setPhotoRef(driver.getPhotoRef());
        kyc.setIdentityRef(driver.getIdentityRef());
        kyc.setRcRef(driver.getRcRef());
        kyc.setLicenseRef(driver.getLicenseRef());
        kyc.setLicenseNumber(driver.getLicenseNumber());
        kyc.setVehicleNumber(driver.getVehicleNumber());
        kyc.setRcNumber(driver.getRcNumber());
        kyc.setVehicleType(blankToNull(request.getVehicleType()));
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
