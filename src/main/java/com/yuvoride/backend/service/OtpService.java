package com.yuvoride.backend.service;

public interface OtpService {
    void sendOtp(String phone);
    void verifyOrThrow(String phone, String otp);
}
