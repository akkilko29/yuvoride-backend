package com.yuvoride.backend.entity;

import com.yuvoride.backend.enums.RideStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "rides",
        indexes = {
                @Index(name = "idx_rides_customer", columnList = "customer_id"),
                @Index(name = "idx_rides_driver", columnList = "driver_id"),
                @Index(name = "idx_rides_status", columnList = "status"),
                @Index(name = "idx_rides_created_at", columnList = "createdAt"),
                @Index(name = "idx_rides_pickup", columnList = "pickupLatitude,pickupLongitude")
        }
)
public class Ride extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @Column(nullable = false)
    private Double pickupLatitude;

    @Column(nullable = false)
    private Double pickupLongitude;

    @Column(nullable = false, length = 500)
    private String pickupAddress;

    @Column(nullable = false)
    private Double destinationLatitude;

    @Column(nullable = false)
    private Double destinationLongitude;

    @Column(nullable = false, length = 500)
    private String destinationAddress;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_type_id", nullable = false)
    private VehicleType vehicleType;

    @Column(nullable = false)
    private Double distanceKm;

    @Column(nullable = false)
    private Integer durationMinutes;

    private Long distanceMeters;
    private Integer durationSeconds;
    private Integer etaSeconds;

    @Column(length = 4000)
    private String polyline;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal estimatedFare;

    @Column(precision = 12, scale = 2)
    private BigDecimal finalFare;

    @Column(precision = 12, scale = 2)
    private BigDecimal driverEarning;

    @Column(precision = 12, scale = 2)
    private BigDecimal commission;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RideStatus status;

    private Instant scheduledAt;
    private Instant requestedAt;
    private Instant acceptedAt;
    private Instant arrivedAt;
    private Instant startedAt;
    private Instant completedAt;
    private Instant cancelledAt;
    private Instant expiresAt;

    @Column(length = 500)
    private String cancellationReason;
}
