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
        TRANSITIONS.put(RideStatus.REQUESTED, EnumSet.of(RideStatus.ACCEPTED, RideStatus.RIDE_CANCELLED, RideStatus.EXPIRED));
        TRANSITIONS.put(RideStatus.SEARCHING_DRIVER, EnumSet.of(RideStatus.ACCEPTED, RideStatus.RIDE_CANCELLED, RideStatus.EXPIRED));
        TRANSITIONS.put(RideStatus.ACCEPTED, EnumSet.of(RideStatus.GOING_TO_PICKUP, RideStatus.RIDE_CANCELLED));
        TRANSITIONS.put(RideStatus.DRIVER_ASSIGNED, EnumSet.of(RideStatus.GOING_TO_PICKUP, RideStatus.RIDE_CANCELLED));
        TRANSITIONS.put(RideStatus.GOING_TO_PICKUP, EnumSet.of(RideStatus.ARRIVED, RideStatus.RIDE_CANCELLED));
        TRANSITIONS.put(RideStatus.DRIVER_ARRIVING, EnumSet.of(RideStatus.ARRIVED, RideStatus.RIDE_CANCELLED));
        TRANSITIONS.put(RideStatus.ARRIVED, EnumSet.of(RideStatus.RIDE_STARTED, RideStatus.RIDE_CANCELLED));
        TRANSITIONS.put(RideStatus.DRIVER_ARRIVED, EnumSet.of(RideStatus.RIDE_STARTED, RideStatus.RIDE_CANCELLED));
        TRANSITIONS.put(RideStatus.RIDE_STARTED, EnumSet.of(RideStatus.GOING_TO_DESTINATION, RideStatus.COMPLETED));
        TRANSITIONS.put(RideStatus.GOING_TO_DESTINATION, EnumSet.of(RideStatus.COMPLETED));
        TRANSITIONS.put(RideStatus.COMPLETED, EnumSet.noneOf(RideStatus.class));
        TRANSITIONS.put(RideStatus.RIDE_COMPLETED, EnumSet.noneOf(RideStatus.class));
        TRANSITIONS.put(RideStatus.RIDE_CANCELLED, EnumSet.noneOf(RideStatus.class));
        TRANSITIONS.put(RideStatus.CANCELLED, EnumSet.noneOf(RideStatus.class));
        TRANSITIONS.put(RideStatus.EXPIRED, EnumSet.noneOf(RideStatus.class));
        TRANSITIONS.put(RideStatus.NO_DRIVER_FOUND, EnumSet.of(RideStatus.RIDE_CANCELLED));
    }

    public void assertTransition(RideStatus from, RideStatus to) {
        RideStatus target = to.canonical();
        Set<RideStatus> allowed = TRANSITIONS.getOrDefault(from, EnumSet.noneOf(RideStatus.class));
        if (!allowed.contains(target) && !allowed.contains(to)) {
            throw new RideStateException("Cannot change ride status from " + from + " to " + to);
        }
    }

    public boolean canCancel(RideStatus status) {
        Set<RideStatus> allowed = TRANSITIONS.getOrDefault(status, EnumSet.noneOf(RideStatus.class));
        return allowed.contains(RideStatus.RIDE_CANCELLED) || allowed.contains(RideStatus.CANCELLED);
    }
}
