package com.yuvoride.backend.dto.admin;

import com.yuvoride.backend.enums.DriverAccountStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminDriverStatusRequest {
    @NotNull
    private DriverAccountStatus accountStatus;
}
