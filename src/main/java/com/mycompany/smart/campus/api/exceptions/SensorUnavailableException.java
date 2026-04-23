package com.mycompany.smart.campus.api.exceptions;

/**
 * Thrown when an operation is attempted on a sensor that is not in ACTIVE state.
 */
public class SensorUnavailableException extends RuntimeException {
    public SensorUnavailableException(String message) {
        super(message);
    }
}
