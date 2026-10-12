package com.yuvoride.backend.enums;

import java.util.Locale;

public enum IdentityType {
    AADHAAR,
    PAN,
    PASSPORT,
    VOTER_ID,
    DRIVING_LICENSE,
    OTHER;

    public static IdentityType from(String value) {
        if (value == null || value.isBlank()) {
            return OTHER;
        }
        try {
            return IdentityType.valueOf(value.trim().toUpperCase(Locale.ROOT).replace(' ', '_'));
        } catch (IllegalArgumentException ex) {
            return OTHER;
        }
    }
}
