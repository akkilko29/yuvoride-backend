package com.yuvoride.backend.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, ErrorCodes.NOT_FOUND, message);
    }
}
