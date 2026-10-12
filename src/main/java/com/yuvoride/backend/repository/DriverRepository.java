package com.yuvoride.backend.repository;

import com.yuvoride.backend.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, Long> {
    @Query("""
            SELECT d FROM Driver d
            JOIN FETCH d.user
            LEFT JOIN FETCH d.vehicle v
            LEFT JOIN FETCH v.vehicleType
            WHERE d.user.id = :userId
            """)
    Optional<Driver> findByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT d FROM Driver d
            JOIN FETCH d.vehicle v
            JOIN FETCH v.vehicleType vt
            WHERE d.online = true
              AND d.available = true
              AND d.status = com.yuvoride.backend.enums.DriverStatus.ONLINE
              AND d.currentLatitude IS NOT NULL
              AND d.currentLongitude IS NOT NULL
              AND vt.id = :vehicleTypeId
            """)
    List<Driver> findAvailableForVehicleType(@Param("vehicleTypeId") Long vehicleTypeId);
}
