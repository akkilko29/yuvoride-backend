package com.yuvoride.backend.repository;

import com.yuvoride.backend.entity.Ride;
import com.yuvoride.backend.enums.RideStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RideRepository extends JpaRepository<Ride, Long> {

    @Query("""
            SELECT r FROM Ride r
            JOIN FETCH r.customer
            JOIN FETCH r.vehicleType
            LEFT JOIN FETCH r.driver d
            LEFT JOIN FETCH d.user
            WHERE r.id = :id
            """)
    Optional<Ride> findDetailedById(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Ride r WHERE r.id = :id")
    Optional<Ride> findByIdForUpdate(@Param("id") Long id);

    Optional<Ride> findByIdAndCustomerId(Long id, Long customerId);

    Optional<Ride> findByIdAndDriverId(Long id, Long driverId);

    Page<Ride> findByCustomerId(Long customerId, Pageable pageable);

    Page<Ride> findByCustomerIdAndStatus(Long customerId, RideStatus status, Pageable pageable);

    Page<Ride> findByDriverId(Long driverId, Pageable pageable);

    Page<Ride> findByDriverIdAndStatus(Long driverId, RideStatus status, Pageable pageable);

    Page<Ride> findByStatus(RideStatus status, Pageable pageable);

    @Query("""
            SELECT COUNT(r) > 0 FROM Ride r
            WHERE r.customer.id = :customerId
              AND r.status IN :activeStatuses
            """)
    boolean existsActiveRideForCustomer(@Param("customerId") Long customerId,
                                        @Param("activeStatuses") Collection<RideStatus> activeStatuses);

    @Query("""
            SELECT COUNT(r) > 0 FROM Ride r
            WHERE r.driver.id = :driverId
              AND r.status IN :activeStatuses
            """)
    boolean existsActiveRideForDriver(@Param("driverId") Long driverId,
                                      @Param("activeStatuses") Collection<RideStatus> activeStatuses);

    Optional<Ride> findFirstByDriverIdAndStatusInOrderByCreatedAtDesc(Long driverId, Collection<RideStatus> statuses);

    @Query("""
            SELECT r FROM Ride r
            JOIN FETCH r.customer
            JOIN FETCH r.vehicleType
            WHERE r.status IN :statuses
              AND r.vehicleType.id = :vehicleTypeId
              AND (r.expiresAt IS NULL OR r.expiresAt > :now)
            ORDER BY r.createdAt DESC
            """)
    List<Ride> findOpenRequests(@Param("vehicleTypeId") Long vehicleTypeId,
                                @Param("statuses") Collection<RideStatus> statuses,
                                @Param("now") Instant now);

    List<Ride> findByDriverIdAndStatusAndCompletedAtGreaterThanEqual(Long driverId, RideStatus status, Instant from);
}
