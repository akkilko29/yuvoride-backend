package com.yuvoride.backend.dto.driver;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatchDriverRequest {
    @Size(min = 2, max = 100)
    private String name;
    private String phone;
    @Size(max = 500)
    private String address;
    private String vehicleNumber;
}
