package com.yuvoride.backend.service.impl;

import com.yuvoride.backend.entity.AdminAccount;
import com.yuvoride.backend.entity.Customer;
import com.yuvoride.backend.entity.Driver;
import com.yuvoride.backend.entity.DriverKyc;
import com.yuvoride.backend.entity.User;
import com.yuvoride.backend.enums.DriverAccountStatus;
import com.yuvoride.backend.enums.DriverStatus;
import com.yuvoride.backend.enums.KycStatus;
import com.yuvoride.backend.enums.UserStatus;
import com.yuvoride.backend.repository.AdminAccountRepository;
import com.yuvoride.backend.repository.CustomerRepository;
import com.yuvoride.backend.repository.DriverKycRepository;
import com.yuvoride.backend.repository.DriverRepository;
import com.yuvoride.backend.service.RoleProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class RoleProfileServiceImpl implements RoleProfileService {

    private final CustomerRepository customerRepository;
    private final AdminAccountRepository adminAccountRepository;
    private final DriverRepository driverRepository;
    private final DriverKycRepository driverKycRepository;

    @Override
    @Transactional
    public void ensure(User user) {
        if (user == null || user.getId() == null || user.getRole() == null) {
            return;
        }
        switch (user.getRole()) {
            case CUSTOMER -> ensureCustomer(user);
            case ADMIN -> ensureAdmin(user);
            case DRIVER -> ensureDriver(user);
        }
    }

    @Override
    @Transactional
    public Driver ensureDriver(User user) {
        return driverRepository.findByUserId(user.getId()).map(existing -> {
            ensureDriverKyc(existing);
            return existing;
        }).orElseGet(() -> {
            Driver created = driverRepository.save(Driver.builder()
                    .user(user)
                    .status(DriverStatus.OFFLINE)
                    .accountStatus(DriverAccountStatus.INCOMPLETE)
                    .kycStatus(KycStatus.PENDING)
                    .online(false)
                    .available(false)
                    .rating(new BigDecimal("5.00"))
                    .totalRides(0)
                    .build());
            ensureDriverKyc(created);
            return created;
        });
    }

    @Override
    @Transactional
    public void ensureDriverKyc(Driver driver) {
        if (driver == null || driver.getId() == null) {
            return;
        }
        if (driverKycRepository.findByDriverId(driver.getId()).isPresent()) {
            return;
        }
        driverKycRepository.save(DriverKyc.builder()
                .driver(driver)
                .dob(driver.getDob())
                .address(driver.getAddress())
                .identityType(driver.getIdentityType())
                .identityNumber(driver.getIdentityNumber())
                .photoRef(driver.getPhotoRef())
                .identityRef(driver.getIdentityRef())
                .rcRef(driver.getRcRef())
                .licenseRef(driver.getLicenseRef())
                .licenseNumber(driver.getLicenseNumber())
                .vehicleNumber(driver.getVehicleNumber())
                .rcNumber(driver.getRcNumber())
                .vehicleType(driver.getPreferredVehicleType() != null
                        ? driver.getPreferredVehicleType().getName()
                        : null)
                .status(driver.getKycStatus() != null ? driver.getKycStatus() : KycStatus.PENDING)
                .submittedAt(java.time.Instant.now())
                .build());
    }

    private void ensureCustomer(User user) {
        if (customerRepository.existsByUserId(user.getId())) {
            return;
        }
        customerRepository.save(Customer.builder()
                .user(user)
                .status(user.getStatus() != null ? user.getStatus() : UserStatus.ACTIVE)
                .build());
    }

    private void ensureAdmin(User user) {
        if (adminAccountRepository.existsByUserId(user.getId())) {
            return;
        }
        adminAccountRepository.save(AdminAccount.builder()
                .user(user)
                .username(user.getEmail() != null ? user.getEmail() : user.getPhone())
                .displayName(user.getName())
                .status(user.getStatus() != null ? user.getStatus() : UserStatus.ACTIVE)
                .build());
    }
}
