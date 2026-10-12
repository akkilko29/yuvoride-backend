package com.yuvoride.backend.websocket;

import java.util.Map;

public record RideEvent(String type, Long rideId, Map<String, Object> payload) {
}
