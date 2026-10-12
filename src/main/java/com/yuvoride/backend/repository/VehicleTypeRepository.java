package com.yuvoride.backend.repository;

import com.yuvoride.backend.entity.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleTypeRepository extends JpaRepository<VehicleType, Long> {
    List<VehicleType> findByActiveTrueOrderByIdAsc();
    Optional<VehicleType> findByNameIgnoreCase(String name);
    Optional<VehicleType> findByIdAndActiveTrue(Long id);
}
