package com.yuvoride.backend.enums;

public enum RideStatus {
    REQUESTED,
    SEARCHING_DRIVER,
    DRIVER_ASSIGNED,
    DRIVER_ARRIVING,
    DRIVER_ARRIVED,
    RIDE_STARTED,
    RIDE_COMPLETED,
    CANCELLED,
    NO_DRIVER_FOUND
}
