package com.yuvoride.backend.service;

public interface RouteService {

    RouteResult getRoute(double pickupLatitude, double pickupLongitude,
                         double destinationLatitude, double destinationLongitude);

    record RouteResult(double distanceKm, int durationMinutes, long distanceMeters, int durationSeconds, String polyline) {
        public RouteResult(double distanceKm, int durationMinutes) {
            this(distanceKm, durationMinutes, Math.round(distanceKm * 1000), Math.max(durationMinutes, 1) * 60, null);
        }
    }
}
