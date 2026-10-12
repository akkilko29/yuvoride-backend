package com.yuvoride.backend.dto.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocationDto {

    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double longitude;

    private Double lat;
    private Double lng;

    @Size(max = 500, message = "Address must be at most 500 characters")
    private String address;

    @Size(max = 500)
    private String label;

    @JsonIgnore
    public Double resolvedLatitude() {
        return latitude != null ? latitude : lat;
    }

    @JsonIgnore
    public Double resolvedLongitude() {
        return longitude != null ? longitude : lng;
    }

    @JsonIgnore
    public String resolvedLabel() {
        if (address != null && !address.isBlank()) {
            return address;
        }
        return label != null ? label : "";
    }

    @AssertTrue(message = "Latitude is required")
    public boolean isLatitudePresent() {
        Double value = resolvedLatitude();
        return value != null && value >= -90 && value <= 90;
    }

    @AssertTrue(message = "Longitude is required")
    public boolean isLongitudePresent() {
        Double value = resolvedLongitude();
        return value != null && value >= -180 && value <= 180;
    }

    @AssertTrue(message = "Address is required")
    public boolean isLabelPresent() {
        return resolvedLabel() != null && !resolvedLabel().isBlank();
    }
}
