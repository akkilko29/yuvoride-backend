package com.yuvoride.backend.service;

public interface RouteService {

    RouteResult getRoute(double pickupLatitude, double pickupLongitude,
                         double destinationLatitude, double destinationLongitude);

    record RouteResult(double distanceKm, int durationMinutes) {
    }
}
