package com.yuvoride.backend.service;

import com.yuvoride.backend.entity.Driver;
import com.yuvoride.backend.entity.Ride;
import com.yuvoride.backend.entity.User;

public interface NotificationService {
    void notifyDriverRideRequest(Driver driver, Ride ride);
    void notifyCustomerDriverAssigned(User customer, Ride ride);
    void notifyRideCancelled(Ride ride);
    void notifyRideStarted(Ride ride);
    void notifyRideCompleted(Ride ride);
    void notifyKycStatusChanged(Driver driver);
}
