package com.yuvoride.backend.service;

import com.yuvoride.backend.enums.RideStatus;
import com.yuvoride.backend.exception.RideStateException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RideStateMachineTest {

    private final RideStateMachine machine = new RideStateMachine();

    @Test
    void allowsHappyPathTransitions() {
        machine.assertTransition(RideStatus.REQUESTED, RideStatus.SEARCHING_DRIVER);
        machine.assertTransition(RideStatus.SEARCHING_DRIVER, RideStatus.DRIVER_ASSIGNED);
        machine.assertTransition(RideStatus.DRIVER_ASSIGNED, RideStatus.DRIVER_ARRIVING);
        machine.assertTransition(RideStatus.DRIVER_ARRIVING, RideStatus.DRIVER_ARRIVED);
        machine.assertTransition(RideStatus.DRIVER_ARRIVED, RideStatus.RIDE_STARTED);
        machine.assertTransition(RideStatus.RIDE_STARTED, RideStatus.RIDE_COMPLETED);
    }

    @Test
    void rejectsInvalidTransition() {
        assertThatThrownBy(() -> machine.assertTransition(RideStatus.RIDE_COMPLETED, RideStatus.CANCELLED))
                .isInstanceOf(RideStateException.class);
    }

    @Test
    void cancellationRules() {
        assertThat(machine.canCancel(RideStatus.SEARCHING_DRIVER)).isTrue();
        assertThat(machine.canCancel(RideStatus.RIDE_STARTED)).isFalse();
        assertThat(machine.canCancel(RideStatus.RIDE_COMPLETED)).isFalse();
    }
}
