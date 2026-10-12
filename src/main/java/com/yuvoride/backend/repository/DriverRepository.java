package com.yuvoride.backend.repository;

import com.yuvoride.backend.entity.Driver;
import com.yuvoride.backend.enums.DriverAccountStatus;
import com.yuvoride.backend.enums.KycStatus;
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
            LEFT JOIN FETCH d.preferredVehicleType
            WHERE d.user.id = :userId
            """)
    Optional<Driver> findByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT d FROM Driver d
            JOIN FETCH d.user
            LEFT JOIN FETCH d.vehicle v
            LEFT JOIN FETCH v.vehicleType vt
            LEFT JOIN FETCH d.preferredVehicleType pvt
            WHERE d.online = true
              AND d.available = true
              AND d.accountStatus = com.yuvoride.backend.enums.DriverAccountStatus.ACTIVE
              AND d.kycStatus = com.yuvoride.backend.enums.KycStatus.VERIFIED
              AND d.status <> com.yuvoride.backend.enums.DriverStatus.BUSY
              AND d.currentLatitude IS NOT NULL
              AND d.currentLongitude IS NOT NULL
              AND (pvt.id = :vehicleTypeId OR vt.id = :vehicleTypeId)
            """)
    List<Driver> findEligibleForVehicleType(@Param("vehicleTypeId") Long vehicleTypeId);

    List<Driver> findByAccountStatusAndKycStatus(DriverAccountStatus accountStatus, KycStatus kycStatus);
}
