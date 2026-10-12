package com.yuvoride.backend.repository;

import com.yuvoride.backend.entity.OtpChallenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface OtpChallengeRepository extends JpaRepository<OtpChallenge, Long> {
    Optional<OtpChallenge> findFirstByPhoneAndConsumedFalseOrderByCreatedAtDesc(String phone);
    long countByPhoneAndCreatedAtAfter(String phone, Instant after);
}
