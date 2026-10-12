package com.yuvoride.backend.util;

import com.yuvoride.backend.exception.BadRequestException;
import org.springframework.util.StringUtils;

import java.util.Locale;

public final class AuthIdentity {

    private AuthIdentity() {
    }

    public static String destination(String phone, String email) {
        boolean hasPhone = StringUtils.hasText(phone);
        boolean hasEmail = StringUtils.hasText(email);
        if (hasPhone == hasEmail) {
            throw new BadRequestException("Provide either phone or email");
        }
        return hasEmail ? normalizeEmail(email) : phone.trim();
    }

    public static boolean isEmail(String destination) {
        return destination != null && destination.contains("@");
    }

    public static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
