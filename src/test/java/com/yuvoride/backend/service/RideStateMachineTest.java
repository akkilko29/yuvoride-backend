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
        machine.assertTransition(RideStatus.REQUESTED, RideStatus.ACCEPTED);
        machine.assertTransition(RideStatus.ACCEPTED, RideStatus.GOING_TO_PICKUP);
        machine.assertTransition(RideStatus.GOING_TO_PICKUP, RideStatus.ARRIVED);
        machine.assertTransition(RideStatus.ARRIVED, RideStatus.RIDE_STARTED);
        machine.assertTransition(RideStatus.RIDE_STARTED, RideStatus.GOING_TO_DESTINATION);
        machine.assertTransition(RideStatus.GOING_TO_DESTINATION, RideStatus.COMPLETED);
    }

    @Test
    void rejectsInvalidTransition() {
        assertThatThrownBy(() -> machine.assertTransition(RideStatus.COMPLETED, RideStatus.RIDE_CANCELLED))
                .isInstanceOf(RideStateException.class);
    }

    @Test
    void cancellationRules() {
        assertThat(machine.canCancel(RideStatus.REQUESTED)).isTrue();
        assertThat(machine.canCancel(RideStatus.SEARCHING_DRIVER)).isTrue();
        assertThat(machine.canCancel(RideStatus.RIDE_STARTED)).isFalse();
        assertThat(machine.canCancel(RideStatus.COMPLETED)).isFalse();
    }
}
