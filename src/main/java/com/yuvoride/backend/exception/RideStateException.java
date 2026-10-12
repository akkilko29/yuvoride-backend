package com.yuvoride.backend.exception;

import org.springframework.http.HttpStatus;

public class RideStateException extends ApiException {
    public RideStateException(String message) {
        super(HttpStatus.CONFLICT, ErrorCodes.INVALID_RIDE_STATE, message);
    }
}
