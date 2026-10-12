package com.yuvoride.backend.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends ApiException {
    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, message);
    }
}
