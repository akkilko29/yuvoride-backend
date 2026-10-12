package com.yuvoride.backend.service.impl;

import com.yuvoride.backend.dto.driver.DriverLocationRequest;
import com.yuvoride.backend.dto.driver.DriverResponse;
import com.yuvoride.backend.dto.driver.KycResponse;
import com.yuvoride.backend.dto.driver.PatchDriverRequest;
import com.yuvoride.backend.dto.driver.UpdateDriverAvailabilityRequest;
import com.yuvoride.backend.dto.driver.UpdateDriverLocationRequest;
import com.yuvoride.backend.dto.driver.UpdateKycRequest;
import com.yuvoride.backend.entity.Driver;
import com.yuvoride.backend.entity.DriverKyc;
import com.yuvoride.backend.enums.DriverAccountStatus;
import com.yuvoride.backend.enums.DriverStatus;
import com.yuvoride.backend.enums.IdentityType;
import com.yuvoride.backend.enums.KycStatus;
import com.yuvoride.backend.enums.RideStatus;
import com.yuvoride.backend.exception.BadRequestException;
import com.yuvoride.backend.exception.ForbiddenException;
import com.yuvoride.backend.exception.UnprocessableException;
import com.yuvoride.backend.exception.ErrorCodes;
import com.yuvoride.backend.mapper.DriverMapper;
import com.yuvoride.backend.mapper.KycMapper;
import com.yuvoride.backend.repository.DriverKycRepository;
import com.yuvoride.backend.repository.DriverRepository;
import com.yuvoride.backend.repository.RideRepository;
import com.yuvoride.backend.service.DriverService;
import com.yuvoride.backend.service.FileStorageService;
import com.yuvoride.backend.service.RoleProfileService;
import com.yuvoride.backend.websocket.RealtimePublisher;
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
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final DriverKycRepository driverKycRepository;
    private final DriverMapper driverMapper;
    private final KycMapper kycMapper;
    private final RideRepository rideRepository;
    private final RealtimePublisher realtimePublisher;
    private final FileStorageService fileStorageService;
    private final RoleProfileService roleProfileService;

    @Override
    @Transactional(readOnly = true)
    public DriverResponse me(Long userId) {
        return driverMapper.toResponse(getDriver(userId));
    }

    @Override
    @Transactional
    public DriverResponse updateProfile(Long userId, PatchDriverRequest request) {
        Driver driver = getDriver(userId);
        if (StringUtils.hasText(request.getName())) {
            driver.getUser().setName(request.getName().trim());
        }
        if (StringUtils.hasText(request.getPhone())) {
            driver.getUser().setPhone(request.getPhone().trim());
        }
        if (request.getAddress() != null) {
            driver.setAddress(request.getAddress());
        }
        if (request.getVehicleNumber() != null) {
            driver.setVehicleNumber(request.getVehicleNumber());
        }
        return driverMapper.toResponse(driverRepository.save(driver));
    }

    @Override
    @Transactional
    public DriverResponse updateLocation(Long userId, UpdateDriverLocationRequest request) {
        DriverLocationRequest body = new DriverLocationRequest();
        body.setLatitude(request.getLatitude());
        body.setLongitude(request.getLongitude());
        return updateLocation(userId, body);
    }

    @Override
    @Transactional
    public DriverResponse updateLocation(Long userId, DriverLocationRequest request) {
        Driver driver = getDriver(userId);
        driver.setCurrentLatitude(request.resolvedLat());
        driver.setCurrentLongitude(request.resolvedLng());
        driver.setLastLocationAt(Instant.now());
        driverRepository.save(driver);
        realtimePublisher.driverLocation(driver);
        return driverMapper.toResponse(driver);
    }

    @Override
    @Transactional
    public DriverResponse updateAvailability(Long userId, UpdateDriverAvailabilityRequest request) {
        Driver driver = getDriver(userId);
        boolean goingOnline = Boolean.TRUE.equals(request.getOnline());
        if (goingOnline && !driver.canGoOnline()) {
            throw new UnprocessableException(ErrorCodes.DRIVER_NOT_ELIGIBLE,
                    "Driver must be ACTIVE and KYC VERIFIED to go online");
        }
        boolean hasActive = rideRepository.existsActiveRideForDriver(driver.getId(), RideStatus.activeStatuses());
        if (!goingOnline && hasActive) {
            throw new ForbiddenException("Cannot go offline during an active ride");
        }
        driver.setOnline(goingOnline);
        if (goingOnline) {
            driver.setStatus(DriverStatus.ONLINE);
            driver.setAvailable(true);
            realtimePublisher.driverOnline(driver);
        } else {
            driver.setStatus(DriverStatus.OFFLINE);
            driver.setAvailable(false);
            realtimePublisher.driverOffline(driver);
        }
        log.info("Driver {} availability online={}", driver.getId(), goingOnline);
        return driverMapper.toResponse(driverRepository.save(driver));
    }

    @Override
    @Transactional(readOnly = true)
    public KycResponse getKyc(Long userId) {
        Driver driver = getDriver(userId);
        roleProfileService.ensureDriverKyc(driver);
        DriverKyc kyc = driverKycRepository.findByDriverId(driver.getId()).orElse(null);
        return kycMapper.toResponse(driver, kyc);
    }

    @Override
    @Transactional
    public KycResponse updateKyc(Long userId, UpdateKycRequest request) {
        Driver driver = getDriver(userId);
        roleProfileService.ensureDriverKyc(driver);
        DriverKyc kyc = driverKycRepository.findByDriverId(driver.getId())
                .orElseThrow(() -> new ForbiddenException("KYC profile not found"));

        if (StringUtils.hasText(request.getName())) {
            driver.getUser().setName(request.getName().trim());
        }
        if (StringUtils.hasText(request.getPhone())) {
            driver.getUser().setPhone(request.getPhone().trim());
        }
        if (StringUtils.hasText(request.getDob())) {
            LocalDate dob = parseDob(request.getDob());
            driver.setDob(dob);
            kyc.setDob(dob);
        }
        if (request.getAddress() != null) {
            driver.setAddress(request.getAddress());
            kyc.setAddress(request.getAddress());
        }
        if (StringUtils.hasText(request.getIdentityType())) {
            IdentityType type = parseIdentity(request.getIdentityType());
            driver.setIdentityType(type);
            kyc.setIdentityType(type);
        }
        if (request.getIdentityNumber() != null) {
            driver.setIdentityNumber(blankToNull(request.getIdentityNumber()));
            kyc.setIdentityNumber(blankToNull(request.getIdentityNumber()));
        }
        if (request.getLicenseNumber() != null) {
            driver.setLicenseNumber(blankToNull(request.getLicenseNumber()));
            kyc.setLicenseNumber(blankToNull(request.getLicenseNumber()));
        }
        if (request.getVehicleNumber() != null) {
            driver.setVehicleNumber(blankToNull(request.getVehicleNumber()));
            kyc.setVehicleNumber(blankToNull(request.getVehicleNumber()));
        }
        if (request.getRcNumber() != null) {
            driver.setRcNumber(blankToNull(request.getRcNumber()));
            kyc.setRcNumber(blankToNull(request.getRcNumber()));
        }
        if (request.getVehicleType() != null) {
            kyc.setVehicleType(blankToNull(request.getVehicleType()));
        }
        applyRef(request.getPhotoUri(), driver::setPhotoRef, kyc::setPhotoRef);
        applyRef(request.getIdentityUri(), driver::setIdentityRef, kyc::setIdentityRef);
        applyRef(request.getRcUri(), driver::setRcRef, kyc::setRcRef);
        applyRef(request.getLicenseUri(), driver::setLicenseRef, kyc::setLicenseRef);

        kyc.setStatus(KycStatus.PENDING);
        kyc.setRejectionReason(null);
        kyc.setSubmittedAt(Instant.now());
        kyc.setReviewedAt(null);
        driver.setKycStatus(KycStatus.PENDING);
        if (driver.getAccountStatus() == DriverAccountStatus.ACTIVE
                || driver.getAccountStatus() == DriverAccountStatus.REJECTED) {
            driver.setAccountStatus(DriverAccountStatus.PENDING);
        }
        driver.setOnline(false);
        driver.setAvailable(false);
        driver.setStatus(DriverStatus.OFFLINE);

        driverKycRepository.save(kyc);
        driverRepository.save(driver);
        log.info("Driver {} submitted KYC for review", driver.getId());
        return kycMapper.toResponse(driver, kyc);
    }

    private void applyRef(String uri, java.util.function.Consumer<String> driverSetter,
                          java.util.function.Consumer<String> kycSetter) {
        if (!StringUtils.hasText(uri)) {
            return;
        }
        String ref = fileStorageService.storeReference(uri);
        driverSetter.accept(ref);
        kycSetter.accept(ref);
    }

    private LocalDate parseDob(String dob) {
        try {
            return LocalDate.parse(dob);
        } catch (Exception ex) {
            throw new BadRequestException("dob must be ISO-8601 (yyyy-MM-dd)");
        }
    }

    private IdentityType parseIdentity(String value) {
        try {
            return IdentityType.valueOf(value.trim().toUpperCase(Locale.ROOT).replace(' ', '_'));
        } catch (IllegalArgumentException ex) {
            return IdentityType.OTHER;
        }
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private Driver getDriver(Long userId) {
        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new ForbiddenException("Driver profile not found"));
        if (driver.getVehicle() != null) {
            driver.getVehicle().getVehicleType();
        }
        driver.getUser();
        return driver;
    }
}
