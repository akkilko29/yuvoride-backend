package com.yuvoride.backend.service;

public interface SmsGateway {
    void sendOtp(String phone);
}
