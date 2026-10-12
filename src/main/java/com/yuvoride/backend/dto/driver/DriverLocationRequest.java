package com.yuvoride.backend.dto.driver;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DriverLocationRequest {
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private Double latitude;
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private Double longitude;
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private Double lat;
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private Double lng;
    private Double accuracy;
    private Double heading;
    private Double speed;

    public Double resolvedLat() {
        return latitude != null ? latitude : lat;
    }

    public Double resolvedLng() {
        return longitude != null ? longitude : lng;
    }

    @AssertTrue(message = "Latitude and longitude are required")
    public boolean isCoordinatePresent() {
        return resolvedLat() != null && resolvedLng() != null;
    }
}
