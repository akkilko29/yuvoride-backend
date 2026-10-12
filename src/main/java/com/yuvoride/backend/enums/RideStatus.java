package com.yuvoride.backend.enums;

import java.util.EnumSet;
import java.util.Set;

public enum RideStatus {
    REQUESTED,
    SEARCHING_DRIVER,
    ACCEPTED,
    DRIVER_ASSIGNED,
    GOING_TO_PICKUP,
    DRIVER_ARRIVING,
    ARRIVED,
    DRIVER_ARRIVED,
    RIDE_STARTED,
    GOING_TO_DESTINATION,
    COMPLETED,
    RIDE_COMPLETED,
    RIDE_CANCELLED,
    CANCELLED,
    EXPIRED,
    NO_DRIVER_FOUND;

    public RideStatus canonical() {
        return switch (this) {
            case SEARCHING_DRIVER -> REQUESTED;
            case DRIVER_ASSIGNED -> ACCEPTED;
            case DRIVER_ARRIVING -> GOING_TO_PICKUP;
            case DRIVER_ARRIVED -> ARRIVED;
            case RIDE_COMPLETED -> COMPLETED;
            case CANCELLED -> RIDE_CANCELLED;
            case NO_DRIVER_FOUND -> EXPIRED;
            default -> this;
        };
    }

    public static Set<RideStatus> activeStatuses() {
        return EnumSet.of(
                REQUESTED,
                SEARCHING_DRIVER,
                ACCEPTED,
                DRIVER_ASSIGNED,
                GOING_TO_PICKUP,
                DRIVER_ARRIVING,
                ARRIVED,
                DRIVER_ARRIVED,
                RIDE_STARTED,
                GOING_TO_DESTINATION
        );
    }

    public boolean isTerminal() {
        return switch (canonical()) {
            case COMPLETED, RIDE_CANCELLED, EXPIRED -> true;
            default -> false;
        };
    }
}
