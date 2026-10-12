package com.yuvoride.backend.service;

import com.yuvoride.backend.enums.RideStatus;
import com.yuvoride.backend.exception.RideStateException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class RideStateMachine {

    private static final Map<RideStatus, Set<RideStatus>> TRANSITIONS = new EnumMap<>(RideStatus.class);

    static {
        TRANSITIONS.put(RideStatus.REQUESTED, EnumSet.of(RideStatus.SEARCHING_DRIVER, RideStatus.CANCELLED));
        TRANSITIONS.put(RideStatus.SEARCHING_DRIVER, EnumSet.of(RideStatus.DRIVER_ASSIGNED, RideStatus.NO_DRIVER_FOUND, RideStatus.CANCELLED));
        TRANSITIONS.put(RideStatus.DRIVER_ASSIGNED, EnumSet.of(RideStatus.DRIVER_ARRIVING, RideStatus.CANCELLED));
        TRANSITIONS.put(RideStatus.DRIVER_ARRIVING, EnumSet.of(RideStatus.DRIVER_ARRIVED, RideStatus.CANCELLED));
        TRANSITIONS.put(RideStatus.DRIVER_ARRIVED, EnumSet.of(RideStatus.RIDE_STARTED, RideStatus.CANCELLED));
        TRANSITIONS.put(RideStatus.RIDE_STARTED, EnumSet.of(RideStatus.RIDE_COMPLETED));
        TRANSITIONS.put(RideStatus.RIDE_COMPLETED, EnumSet.noneOf(RideStatus.class));
        TRANSITIONS.put(RideStatus.CANCELLED, EnumSet.noneOf(RideStatus.class));
        TRANSITIONS.put(RideStatus.NO_DRIVER_FOUND, EnumSet.of(RideStatus.CANCELLED));
    }

    public void assertTransition(RideStatus from, RideStatus to) {
        Set<RideStatus> allowed = TRANSITIONS.getOrDefault(from, EnumSet.noneOf(RideStatus.class));
        if (!allowed.contains(to)) {
            throw new RideStateException("Cannot change ride status from " + from + " to " + to);
        }
    }

    public boolean canCancel(RideStatus status) {
        return TRANSITIONS.getOrDefault(status, EnumSet.noneOf(RideStatus.class)).contains(RideStatus.CANCELLED);
    }
}
