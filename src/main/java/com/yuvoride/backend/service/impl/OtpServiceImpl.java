package com.yuvoride.backend.service.impl;

import com.yuvoride.backend.config.AppProperties;
import com.yuvoride.backend.entity.OtpChallenge;
import com.yuvoride.backend.exception.BadRequestException;
import com.yuvoride.backend.exception.RateLimitException;
import com.yuvoride.backend.repository.OtpChallengeRepository;
import com.yuvoride.backend.service.OtpCodeGenerator;
import com.yuvoride.backend.service.OtpService;
import com.yuvoride.backend.service.SmsGateway;
import com.yuvoride.backend.util.HashUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final OtpChallengeRepository otpChallengeRepository;
    private final SmsGateway smsGateway;
    private final OtpCodeGenerator otpCodeGenerator;
    private final AppProperties properties;

    @Override
    @Transactional
    public void sendOtp(String phone) {
        Instant now = Instant.now();
        long sendsLastHour = otpChallengeRepository.countByPhoneAndCreatedAtAfter(phone, now.minus(1, ChronoUnit.HOURS));
        if (sendsLastHour >= properties.getOtp().getMaxSendsPerHour()) {
            throw new RateLimitException("Too many OTP requests. Try again later.");
        }

        otpChallengeRepository.findFirstByPhoneAndConsumedFalseOrderByCreatedAtDesc(phone).ifPresent(existing -> {
            long cooldown = properties.getOtp().getResendCooldownSeconds();
            if (existing.getCreatedAt().plusSeconds(cooldown).isAfter(now)) {
                throw new RateLimitException("Please wait before requesting another OTP");
            }
        });

        persistAndDispatch(phone, now);
    }

    @Override
    @Transactional
    public void verifyOrThrow(String phone, String otp) {
        OtpChallenge challenge = otpChallengeRepository.findFirstByPhoneAndConsumedFalseOrderByCreatedAtDesc(phone)
                .orElseThrow(() -> new BadRequestException("OTP not found. Request a new code."));

        Instant now = Instant.now();
        if (challenge.getExpiresAt().isBefore(now)) {
            throw new BadRequestException("OTP has expired");
        }
        if (challenge.getAttempts() >= properties.getOtp().getMaxAttempts()) {
            throw new BadRequestException("Maximum OTP attempts exceeded");
        }

        challenge.setAttempts(challenge.getAttempts() + 1);
        if (!challenge.getCodeHash().equals(hash(phone, otp))) {
            otpChallengeRepository.save(challenge);
            throw new BadRequestException("Invalid OTP");
        }
        challenge.setConsumed(true);
        otpChallengeRepository.save(challenge);
    }

    private void persistAndDispatch(String phone, Instant now) {
        String code = otpCodeGenerator.generate(properties.getOtp().getLength());
        OtpChallenge challenge = OtpChallenge.builder()
                .phone(phone)
                .codeHash(hash(phone, code))
                .expiresAt(now.plus(properties.getOtp().getExpirationMinutes(), ChronoUnit.MINUTES))
                .createdAt(now)
                .attempts(0)
                .consumed(false)
                .build();
        otpChallengeRepository.save(challenge);
        smsGateway.sendOtp(phone);
        log.info("OTP challenge created for phone ending {}", phone.substring(Math.max(0, phone.length() - 4)));
    }

    public static String hash(String phone, String otp) {
        return HashUtils.sha256(phone + ":" + otp);
    }
}
