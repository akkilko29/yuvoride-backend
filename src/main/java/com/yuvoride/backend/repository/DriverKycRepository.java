package com.yuvoride.backend.repository;

import com.yuvoride.backend.entity.DriverKyc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DriverKycRepository extends JpaRepository<DriverKyc, Long> {
    Optional<DriverKyc> findByDriverId(Long driverId);
}
