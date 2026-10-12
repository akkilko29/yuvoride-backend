package com.yuvoride.backend.service.impl;

import com.yuvoride.backend.entity.Driver;
import com.yuvoride.backend.entity.Ride;
import com.yuvoride.backend.entity.User;
import com.yuvoride.backend.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LoggingNotificationService implements NotificationService {

    @Override
    public void notifyDriverRideRequest(Driver driver, Ride ride) {
        log.info("Notify driver {} of ride request {}", driver.getId(), ride.getId());
    }

    @Override
    public void notifyCustomerDriverAssigned(User customer, Ride ride) {
        log.info("Notify customer {} that ride {} was assigned", customer.getId(), ride.getId());
    }

    @Override
    public void notifyRideCancelled(Ride ride) {
        log.info("Notify parties that ride {} was cancelled", ride.getId());
    }

    @Override
    public void notifyRideStarted(Ride ride) {
        log.info("Notify parties that ride {} started", ride.getId());
    }

    @Override
    public void notifyRideCompleted(Ride ride) {
        log.info("Notify parties that ride {} completed", ride.getId());
    }

    @Override
    public void notifyKycStatusChanged(Driver driver) {
        log.info("Notify driver {} of KYC status {}", driver.getId(), driver.getKycStatus());
    }
}
