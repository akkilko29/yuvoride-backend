package com.yuvoride.backend.exception;

public final class ErrorCodes {

    public static final String VALIDATION_FAILED = "VALIDATION_FAILED";
    public static final String UNAUTHORIZED = "UNAUTHORIZED";
    public static final String FORBIDDEN = "FORBIDDEN";
    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String CONFLICT = "CONFLICT";
    public static final String RIDE_ALREADY_TAKEN = "RIDE_ALREADY_TAKEN";
    public static final String INVALID_RIDE_STATE = "INVALID_RIDE_STATE";
    public static final String DRIVER_NOT_ELIGIBLE = "DRIVER_NOT_ELIGIBLE";
    public static final String RATE_LIMITED = "RATE_LIMITED";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";

    private ErrorCodes() {
    }
}
