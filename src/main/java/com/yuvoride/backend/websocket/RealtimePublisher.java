package com.yuvoride.backend.websocket;

import com.yuvoride.backend.entity.Driver;
import com.yuvoride.backend.entity.Ride;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RealtimePublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public RealtimePublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void driverOnline(Driver driver) {
        send("/topic/driver/" + driver.getId(), new RideEvent("driver:online", null, Map.of("driverId", driver.getId())));
    }

    public void driverOffline(Driver driver) {
        send("/topic/driver/" + driver.getId(), new RideEvent("driver:offline", null, Map.of("driverId", driver.getId())));
    }

    public void driverLocation(Driver driver) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("driverId", driver.getId());
        payload.put("lat", driver.getCurrentLatitude());
        payload.put("lng", driver.getCurrentLongitude());
        payload.put("at", driver.getLastLocationAt());
        send("/topic/driver/" + driver.getId() + "/location", new RideEvent("driver:location", null, payload));
        if (driver.getId() != null) {
            send("/topic/ride/driver-location/" + driver.getId(), new RideEvent("ride:driver_location", null, payload));
        }
    }

    public void rideOfferedToDrivers(Ride ride, List<Driver> drivers) {
        Map<String, Object> payload = ridePayload(ride);
        for (Driver driver : drivers) {
            send("/topic/driver/" + driver.getId() + "/rides", new RideEvent("ride:new", ride.getId(), payload));
        }
    }

    public void rideAccepted(Ride ride) {
        RideEvent event = new RideEvent("ride:accepted", ride.getId(), ridePayload(ride));
        sendRide(ride, event);
        send("/topic/ride/" + ride.getId(), new RideEvent("ride:driver_assigned", ride.getId(), ridePayload(ride)));
    }

    public void rideStatusChanged(Ride ride, String type) {
        sendRide(ride, new RideEvent(type, ride.getId(), ridePayload(ride)));
        send("/topic/ride/" + ride.getId(), new RideEvent("ride:status_changed", ride.getId(), ridePayload(ride)));
    }

    private Map<String, Object> ridePayload(Ride ride) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", ride.getId());
        payload.put("status", ride.getStatus().name());
        payload.put("vehicleType", ride.getVehicleType() != null ? ride.getVehicleType().getName() : null);
        payload.put("estimatedFare", ride.getEstimatedFare());
        payload.put("pickupLatitude", ride.getPickupLatitude());
        payload.put("pickupLongitude", ride.getPickupLongitude());
        payload.put("destinationLatitude", ride.getDestinationLatitude());
        payload.put("destinationLongitude", ride.getDestinationLongitude());
        if (ride.getDriver() != null) {
            payload.put("driverId", ride.getDriver().getId());
        }
        return payload;
    }

    private void sendRide(Ride ride, RideEvent event) {
        send("/topic/ride/" + ride.getId(), event);
        send("/topic/customer/" + ride.getCustomer().getId() + "/rides", event);
        if (ride.getDriver() != null) {
            send("/topic/driver/" + ride.getDriver().getId() + "/rides", event);
        }
    }

    private void send(String destination, RideEvent event) {
        try {
            messagingTemplate.convertAndSend(destination, event);
        } catch (Exception ex) {
            log.warn("Failed to publish realtime event {}", event.type());
        }
    }
}
