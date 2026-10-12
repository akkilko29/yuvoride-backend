package com.yuvoride.backend.config;

import com.yuvoride.backend.entity.VehicleType;
import com.yuvoride.backend.repository.VehicleTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final VehicleTypeRepository vehicleTypeRepository;

    @Override
    public void run(String... args) {
        seedVehicle("Rapid", "Quick and economical bike-taxi style ride", 1, "30", "8", "0", "40");
        seedVehicle("Mini", "Affordable compact car", 4, "50", "15", "0", "80");
        seedVehicle("Sedan", "Comfortable sedan ride", 4, "80", "18", "2", "120");
        seedVehicle("SUV", "Spacious SUV for groups", 6, "120", "22", "2", "180");
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
