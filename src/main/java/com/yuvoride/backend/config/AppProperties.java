package com.yuvoride.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Jwt jwt = new Jwt();
    private final Otp otp = new Otp();
    private final Google google = new Google();
    private final Cors cors = new Cors();
    private final Storage storage = new Storage();
    private final DriverMatch driverMatch = new DriverMatch();
    private String currency = "INR";

    @Getter
    @Setter
    public static class Jwt {
        private String secret;
        private long accessTokenExpirationMs = 900_000;
        private long refreshTokenExpirationMs = 604_800_000;
    }

    @Getter
    @Setter
    public static class Otp {
        private int expirationMinutes = 5;
        private int resendCooldownSeconds = 60;
        private int maxAttempts = 5;
        private int maxSendsPerHour = 5;
        private int length = 6;
    }

    @Getter
    @Setter
    public static class Google {
        private String clientId;
        private String mapsApiKey;
    }

    @Getter
    @Setter
    public static class Cors {
        private String allowedOrigins = "http://localhost:8081";
    }

    @Getter
    @Setter
    public static class Storage {
        private String localDir = "uploads";
        private String publicBaseUrl = "http://localhost:8080/files";
    }

    @Getter
    @Setter
    public static class DriverMatch {
        private double radiusKm = 10;
        private int requestTtlSeconds = 120;
        private int locationMaxAgeSeconds = 300;
    }
}
