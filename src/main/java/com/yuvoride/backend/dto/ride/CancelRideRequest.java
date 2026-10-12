package com.yuvoride.backend.dto.ride;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancelRideRequest {

    @NotBlank(message = "Cancellation reason is required")
    @Size(max = 500, message = "Reason must be at most 500 characters")
    private String reason;
}
