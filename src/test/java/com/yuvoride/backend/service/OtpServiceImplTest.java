package com.yuvoride.backend.service;

import com.yuvoride.backend.config.AppProperties;
import com.yuvoride.backend.entity.OtpChallenge;
import com.yuvoride.backend.exception.BadRequestException;
import com.yuvoride.backend.exception.RateLimitException;
import com.yuvoride.backend.repository.OtpChallengeRepository;
import com.yuvoride.backend.service.impl.OtpServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OtpServiceImplTest {

    @Mock
    private OtpChallengeRepository otpChallengeRepository;
    @Mock
    private SmsGateway smsGateway;
    @Mock
    private OtpCodeGenerator otpCodeGenerator;

    private OtpServiceImpl otpService;

    @BeforeEach
    void setUp() {
        AppProperties properties = new AppProperties();
        otpService = new OtpServiceImpl(otpChallengeRepository, smsGateway, otpCodeGenerator, properties);
    }

    @Test
    void sendOtpPersistsHashedCode() {
        when(otpChallengeRepository.countByPhoneAndCreatedAtAfter(any(), any())).thenReturn(0L);
        when(otpChallengeRepository.findFirstByPhoneAndConsumedFalseOrderByCreatedAtDesc(any()))
                .thenReturn(Optional.empty());
        when(otpCodeGenerator.generate(6)).thenReturn("123456");

        otpService.sendOtp("+919999999999");

        ArgumentCaptor<OtpChallenge> captor = ArgumentCaptor.forClass(OtpChallenge.class);
        org.mockito.Mockito.verify(otpChallengeRepository).save(captor.capture());
        assertThat(captor.getValue().getCodeHash()).isEqualTo(OtpServiceImpl.hash("+919999999999", "123456"));
        assertThat(captor.getValue().getCodeHash()).doesNotContain("123456");
    }

    @Test
    void rejectsExpiredOtp() {
        OtpChallenge challenge = OtpChallenge.builder()
                .phone("+919999999999")
                .codeHash(OtpServiceImpl.hash("+919999999999", "123456"))
                .expiresAt(Instant.now().minusSeconds(10))
                .createdAt(Instant.now().minusSeconds(400))
                .attempts(0)
                .consumed(false)
                .build();
        when(otpChallengeRepository.findFirstByPhoneAndConsumedFalseOrderByCreatedAtDesc("+919999999999"))
                .thenReturn(Optional.of(challenge));

        assertThatThrownBy(() -> otpService.verifyOrThrow("+919999999999", "123456"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void rejectsInvalidOtpAndIncrementsAttempts() {
        OtpChallenge challenge = OtpChallenge.builder()
                .phone("+919999999999")
                .codeHash(OtpServiceImpl.hash("+919999999999", "123456"))
                .expiresAt(Instant.now().plusSeconds(120))
                .createdAt(Instant.now())
                .attempts(0)
                .consumed(false)
                .build();
        when(otpChallengeRepository.findFirstByPhoneAndConsumedFalseOrderByCreatedAtDesc("+919999999999"))
                .thenReturn(Optional.of(challenge));

        assertThatThrownBy(() -> otpService.verifyOrThrow("+919999999999", "000000"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid OTP");
        assertThat(challenge.getAttempts()).isEqualTo(1);
    }

    @Test
    void enforcesSendRateLimit() {
        when(otpChallengeRepository.countByPhoneAndCreatedAtAfter(any(), any())).thenReturn(5L);

        assertThatThrownBy(() -> otpService.sendOtp("+919999999999"))
                .isInstanceOf(RateLimitException.class);
    }

    @Test
    void verifiesMatchingOtp() {
        OtpChallenge challenge = OtpChallenge.builder()
                .phone("+919999999999")
                .codeHash(OtpServiceImpl.hash("+919999999999", "123456"))
                .expiresAt(Instant.now().plusSeconds(120))
                .createdAt(Instant.now())
                .attempts(0)
                .consumed(false)
                .build();
        when(otpChallengeRepository.findFirstByPhoneAndConsumedFalseOrderByCreatedAtDesc("+919999999999"))
                .thenReturn(Optional.of(challenge));

        otpService.verifyOrThrow("+919999999999", "123456");

        assertThat(challenge.isConsumed()).isTrue();
    }
}
