package com.yuvoride.backend.service.impl;

import com.yuvoride.backend.service.SmsGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MockSmsGateway implements SmsGateway {

    @Override
    public void sendOtp(String phone) {
        String suffix = phone.length() > 4 ? phone.substring(phone.length() - 4) : phone;
        log.info("OTP dispatched via mock SMS gateway to phone ending {}", suffix);
    }
}
