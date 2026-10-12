package com.yuvoride.backend.config;

import com.yuvoride.backend.entity.AdminAccount;
import com.yuvoride.backend.entity.User;
import com.yuvoride.backend.entity.VehicleType;
import com.yuvoride.backend.enums.AuthProvider;
import com.yuvoride.backend.enums.UserRole;
import com.yuvoride.backend.enums.UserStatus;
import com.yuvoride.backend.repository.AdminAccountRepository;
import com.yuvoride.backend.repository.UserRepository;
import com.yuvoride.backend.repository.VehicleTypeRepository;
import com.yuvoride.backend.service.RoleProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final VehicleTypeRepository vehicleTypeRepository;
    private final UserRepository userRepository;
    private final AdminAccountRepository adminAccountRepository;
    private final RoleProfileService roleProfileService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.phone:}")
    private String adminPhone;

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.username:}")
    private String adminUsername;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Value("${app.admin.name:YuvoRide Admin}")
    private String adminName;

    @Override
    public void run(String... args) {
        seedVehicle("Rapid", "Quick and economical bike-taxi style ride", 1, "30", "8", "0", "40");
        seedVehicle("Mini", "Affordable compact car", 4, "50", "15", "0", "80");
        seedVehicle("Sedan", "Comfortable sedan ride", 4, "80", "18", "2", "120");
        seedVehicle("SUV", "Spacious SUV for groups", 6, "120", "22", "2", "180");
        seedAdmin();
        userRepository.findAll().forEach(roleProfileService::ensure);
    }

    private void seedAdmin() {
        if (!StringUtils.hasText(adminUsername) && !StringUtils.hasText(adminEmail) && !StringUtils.hasText(adminPhone)) {
            return;
        }
        User admin = null;
        if (StringUtils.hasText(adminEmail)) {
            admin = userRepository.findByEmail(adminEmail.trim().toLowerCase()).orElse(null);
        }
        if (admin == null && StringUtils.hasText(adminPhone)) {
            admin = userRepository.findByPhone(adminPhone.trim()).orElse(null);
        }
        if (admin == null) {
            admin = userRepository.save(User.builder()
                    .name(adminName)
                    .phone(StringUtils.hasText(adminPhone) ? adminPhone.trim() : null)
                    .email(StringUtils.hasText(adminEmail) ? adminEmail.trim().toLowerCase() : null)
                    .role(UserRole.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .authProvider(StringUtils.hasText(adminPassword) ? AuthProvider.PASSWORD : AuthProvider.OTP)
                    .build());
            log.info("Seeded admin user userId={}", admin.getId());
        }
        if (admin.getRole() != UserRole.ADMIN) {
            return;
        }
        if (StringUtils.hasText(adminPassword) && !StringUtils.hasText(admin.getPasswordHash())) {
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
            admin.setAuthProvider(AuthProvider.PASSWORD);
            userRepository.save(admin);
        }
        roleProfileService.ensure(admin);
        AdminAccount account = adminAccountRepository.findByUserId(admin.getId()).orElse(null);
        if (account != null && StringUtils.hasText(adminUsername)) {
            account.setUsername(adminUsername.trim());
            adminAccountRepository.save(account);
        }
    }

    private void seedVehicle(String name, String description, int capacity,
                             String baseFare, String perKm, String perMinute, String minimum) {
        if (vehicleTypeRepository.findByNameIgnoreCase(name).isPresent()) {
            return;
        }
        VehicleType type = VehicleType.builder()
                .name(name)
                .description(description)
                .capacity(capacity)
                .baseFare(new BigDecimal(baseFare))
                .perKmRate(new BigDecimal(perKm))
                .perMinuteRate(new BigDecimal(perMinute))
                .minimumFare(new BigDecimal(minimum))
                .commissionPercent(new BigDecimal("20"))
                .active(true)
                .build();
        vehicleTypeRepository.save(type);
        log.info("Seeded vehicle type {}", name);
    }
}
