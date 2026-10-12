package com.yuvoride.backend.entity;

import com.yuvoride.backend.enums.DriverAccountStatus;
import com.yuvoride.backend.enums.DriverStatus;
import com.yuvoride.backend.enums.IdentityType;
import com.yuvoride.backend.enums.KycStatus;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "drivers",
        indexes = {
                @Index(name = "idx_drivers_online", columnList = "online"),
                @Index(name = "idx_drivers_available", columnList = "available"),
                @Index(name = "idx_drivers_status", columnList = "status"),
                @Index(name = "idx_drivers_account_status", columnList = "accountStatus"),
                @Index(name = "idx_drivers_kyc_status", columnList = "kycStatus"),
                @Index(name = "idx_drivers_location", columnList = "currentLatitude,currentLongitude")
        }
)
public class Driver extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private LocalDate dob;

    @Column(length = 500)
    private String address;

    @Column(unique = true, length = 50)
    private String licenseNumber;

    @Column(length = 30)
    private String vehicleNumber;

    @Column(length = 50)
    private String rcNumber;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private IdentityType identityType;

    @Column(length = 50)
    private String identityNumber;

    @Column(length = 500)
    private String photoRef;

    @Column(length = 500)
    private String identityRef;

    @Column(length = 500)
    private String rcRef;

    @Column(length = 500)
    private String licenseRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferred_vehicle_type_id")
    private VehicleType preferredVehicleType;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DriverStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DriverAccountStatus accountStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private KycStatus kycStatus;

    @Column(nullable = false)
    private boolean online;

    @Column(nullable = false)
    private boolean available;

    private Double currentLatitude;

    private Double currentLongitude;

    private Instant lastLocationAt;

    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal rating;

    @Column(nullable = false)
    private Integer totalRides;

    public boolean canGoOnline() {
        return accountStatus == DriverAccountStatus.ACTIVE && kycStatus == KycStatus.VERIFIED;
    }
}
