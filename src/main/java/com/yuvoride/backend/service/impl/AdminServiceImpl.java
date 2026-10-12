package com.yuvoride.backend.service.impl;

import com.yuvoride.backend.dto.admin.AdminDriverStatusRequest;
import com.yuvoride.backend.dto.admin.AdminKycRequest;
import com.yuvoride.backend.dto.driver.DriverResponse;
import com.yuvoride.backend.dto.ride.RideResponse;
import com.yuvoride.backend.dto.user.UserResponse;
import com.yuvoride.backend.entity.Driver;
import com.yuvoride.backend.entity.DriverKyc;
import com.yuvoride.backend.enums.DriverAccountStatus;
import com.yuvoride.backend.enums.DriverStatus;
import com.yuvoride.backend.enums.KycStatus;
import com.yuvoride.backend.exception.ResourceNotFoundException;
import com.yuvoride.backend.mapper.DriverMapper;
import com.yuvoride.backend.mapper.RideMapper;
import com.yuvoride.backend.mapper.UserMapper;
import com.yuvoride.backend.repository.CustomerRepository;
import com.yuvoride.backend.repository.DriverKycRepository;
import com.yuvoride.backend.repository.DriverRepository;
import com.yuvoride.backend.repository.RideRepository;
import com.yuvoride.backend.repository.UserRepository;
import com.yuvoride.backend.service.AdminService;
import com.yuvoride.backend.service.NotificationService;
import com.yuvoride.backend.service.RoleProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final DriverRepository driverRepository;
    private final DriverKycRepository driverKycRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final RideRepository rideRepository;
    private final RoleProfileService roleProfileService;
    private final DriverMapper driverMapper;
    private final UserMapper userMapper;
    private final RideMapper rideMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public List<DriverResponse> listDrivers() {
        return driverRepository.findAll().stream().map(driverMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DriverResponse getDriver(Long id) {
        return driverMapper.toResponse(load(id));
    }

    @Override
    @Transactional
    public DriverResponse updateKyc(Long id, AdminKycRequest request) {
        Driver driver = load(id);
        driver.setKycStatus(request.getKycStatus());
        if (request.getKycStatus() == KycStatus.VERIFIED) {
            driver.setAccountStatus(DriverAccountStatus.ACTIVE);
        } else if (request.getKycStatus() == KycStatus.REJECTED) {
            driver.setAccountStatus(DriverAccountStatus.REJECTED);
            driver.setOnline(false);
            driver.setAvailable(false);
            driver.setStatus(DriverStatus.OFFLINE);
        }
        roleProfileService.ensureDriverKyc(driver);
        DriverKyc kyc = driverKycRepository.findByDriverId(driver.getId()).orElse(null);
        if (kyc != null) {
            kyc.setStatus(request.getKycStatus());
            kyc.setRejectionReason(request.getReason());
            kyc.setReviewedAt(Instant.now());
            driverKycRepository.save(kyc);
        }
        driverRepository.save(driver);
        notificationService.notifyKycStatusChanged(driver);
        log.info("Admin set driver {} KYC to {}", id, request.getKycStatus());
        return driverMapper.toResponse(driver);
    }

    @Override
    @Transactional
    public DriverResponse updateStatus(Long id, AdminDriverStatusRequest request) {
        Driver driver = load(id);
        driver.setAccountStatus(request.getAccountStatus());
        if (request.getAccountStatus() == DriverAccountStatus.SUSPENDED
                || request.getAccountStatus() == DriverAccountStatus.REJECTED) {
            driver.setOnline(false);
            driver.setAvailable(false);
            driver.setStatus(DriverStatus.SUSPENDED);
        }
        driverRepository.save(driver);
        log.info("Admin set driver {} account status to {}", id, request.getAccountStatus());
        return driverMapper.toResponse(driver);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toResponse).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> listCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable).map(customer -> userMapper.toResponse(customer.getUser())).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RideResponse> listRides(Pageable pageable) {
        return rideRepository.findAll(pageable).map(rideMapper::toResponse).getContent();
    }

    private Driver load(Long id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));
    }
}
