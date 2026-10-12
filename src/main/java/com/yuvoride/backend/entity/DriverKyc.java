package com.yuvoride.backend.entity;

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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "driver_kyc")
public class DriverKyc extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driver_id", nullable = false, unique = true)
    private Driver driver;

    private LocalDate dob;

    @Column(length = 500)
    private String address;

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

    @Column(length = 50)
    private String licenseNumber;

    @Column(length = 30)
    private String vehicleNumber;

    @Column(length = 50)
    private String rcNumber;

    @Column(length = 50)
    private String vehicleType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private KycStatus status;

    @Column(length = 500)
    private String rejectionReason;

    private Instant submittedAt;

    private Instant reviewedAt;
}
