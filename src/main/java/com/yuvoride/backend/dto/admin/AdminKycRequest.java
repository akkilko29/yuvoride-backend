package com.yuvoride.backend.dto.admin;

import com.yuvoride.backend.enums.KycStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminKycRequest {
    @NotNull
    private KycStatus kycStatus;
    private String reason;
}
