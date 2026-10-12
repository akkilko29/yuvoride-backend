package com.yuvoride.backend.service.impl;

import tools.jackson.databind.JsonNode;
import com.yuvoride.backend.config.AppProperties;
import com.yuvoride.backend.exception.BadRequestException;
import com.yuvoride.backend.exception.ServiceUnavailableException;
import com.yuvoride.backend.service.RouteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
public class GoogleRouteService implements RouteService {

    private final AppProperties properties;
    private final RestClient restClient;

    public GoogleRouteService(AppProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.create();
    }

    @Override
    public RouteResult getRoute(double pickupLatitude, double pickupLongitude,
                                double destinationLatitude, double destinationLongitude) {
        String apiKey = properties.getGoogle().getMapsApiKey();
        if (!StringUtils.hasText(apiKey)) {
            throw new ServiceUnavailableException(
                    "Google Maps routing is not configured. Set GOOGLE_MAPS_API_KEY.");
        }

        String origin = pickupLatitude + "," + pickupLongitude;
        String destination = destinationLatitude + "," + destinationLongitude;
        try {
            JsonNode body = restClient.get()
                    .uri("https://maps.googleapis.com/maps/api/directions/json?origin={origin}&destination={destination}&key={key}",
                            origin, destination, apiKey)
                    .retrieve()
                    .body(JsonNode.class);
            if (body == null) {
                throw new ServiceUnavailableException("Routing service returned an empty response");
            }
            String status = body.path("status").asText();
            if ("ZERO_RESULTS".equals(status) || "NOT_FOUND".equals(status)) {
                throw new BadRequestException("No route found between the given locations");
            }
            if (!"OK".equals(status)) {
                log.warn("Google Directions API status={}", status);
                throw new ServiceUnavailableException("Routing service is currently unavailable");
            }
            JsonNode leg = body.path("routes").path(0).path("legs").path(0);
            double meters = leg.path("distance").path("value").asDouble();
            int seconds = leg.path("duration").path("value").asInt();
            if (meters <= 0) {
                throw new BadRequestException("No route found between the given locations");
            }
            double distanceKm = Math.round((meters / 1000.0) * 10.0) / 10.0;
            int durationMinutes = (int) Math.ceil(seconds / 60.0);
            return new RouteResult(distanceKm, Math.max(durationMinutes, 1));
        } catch (BadRequestException | ServiceUnavailableException ex) {
            throw ex;
        } catch (RestClientException ex) {
            log.error("Failed to call Google Directions API");
            throw new ServiceUnavailableException("Routing service is currently unavailable");
        }
    }
}
