package com.yuvoride.backend.dto.driver;

import com.yuvoride.backend.enums.RideStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RideStatusUpdateRequest {
    @NotNull
    private RideStatus status;
}
