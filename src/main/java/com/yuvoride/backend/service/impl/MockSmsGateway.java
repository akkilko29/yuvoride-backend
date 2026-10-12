package com.yuvoride.backend.service.impl;

import com.yuvoride.backend.service.OtpProvider;
import com.yuvoride.backend.service.SmsGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MockSmsGateway implements SmsGateway, OtpProvider {

    @Override
    public void sendOtp(String phone) {
        send(phone);
    }

    @Override
    public void send(String destination) {
        String suffix = destination.length() > 4 ? destination.substring(destination.length() - 4) : destination;
        log.info("OTP dispatched via mock provider to destination ending {}", suffix);
    }
}
