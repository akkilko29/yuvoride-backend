package com.yuvoride.backend.dto.driver;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDriverAvailabilityRequest {

    @NotNull(message = "online is required")
    private Boolean online;
}
