package com.yuvoride.backend.config;

import com.yuvoride.backend.entity.User;
import com.yuvoride.backend.entity.VehicleType;
import com.yuvoride.backend.enums.AuthProvider;
import com.yuvoride.backend.enums.UserRole;
import com.yuvoride.backend.enums.UserStatus;
import com.yuvoride.backend.repository.UserRepository;
import com.yuvoride.backend.repository.VehicleTypeRepository;
import com.yuvoride.backend.service.RoleProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final VehicleTypeRepository vehicleTypeRepository;
    private final UserRepository userRepository;
    private final RoleProfileService roleProfileService;

    @Value("${app.admin.phone:}")
    private String adminPhone;

    @Value("${app.admin.email:}")
    private String adminEmail;

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
        if (!StringUtils.hasText(adminPhone) && !StringUtils.hasText(adminEmail)) {
            return;
        }
        if (StringUtils.hasText(adminPhone) && userRepository.findByPhone(adminPhone.trim()).isPresent()) {
            return;
        }
        if (StringUtils.hasText(adminEmail) && userRepository.findByEmail(adminEmail.trim().toLowerCase()).isPresent()) {
            return;
        }
        User admin = userRepository.save(User.builder()
                .name(adminName)
                .phone(StringUtils.hasText(adminPhone) ? adminPhone.trim() : null)
                .email(StringUtils.hasText(adminEmail) ? adminEmail.trim().toLowerCase() : null)
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .authProvider(AuthProvider.OTP)
                .build());
        roleProfileService.ensure(admin);
        log.info("Seeded admin account userId={}", admin.getId());
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
