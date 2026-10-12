package com.yuvoride.backend.service;

import com.yuvoride.backend.util.HashUtils;
import org.springframework.stereotype.Component;

@Component
public class OtpCodeGenerator {
    public String generate(int length) {
        return HashUtils.numericCode(length);
    }
}
