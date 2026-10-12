package com.yuvoride.backend.service;

import com.yuvoride.backend.entity.Ride;

public interface DriverMatchingService {
    void assignDriver(Ride ride);
}
