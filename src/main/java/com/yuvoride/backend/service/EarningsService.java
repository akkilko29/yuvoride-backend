package com.yuvoride.backend.service;

import com.yuvoride.backend.dto.driver.EarningsResponse;

public interface EarningsService {
    EarningsResponse forDriver(Long userId);
}
