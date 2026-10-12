package com.yuvoride.backend.service;

import com.yuvoride.backend.entity.Driver;
import com.yuvoride.backend.entity.Ride;

import java.util.List;

public interface DriverMatchingService {
    List<Driver> findEligibleDrivers(Ride ride);

    void offerRide(Ride ride);
}
