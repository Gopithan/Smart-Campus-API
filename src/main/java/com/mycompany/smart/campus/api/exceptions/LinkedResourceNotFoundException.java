package com.mycompany.smart.campus.api.exceptions;

/**
 * Thrown when a resource depends on another resource that does not exist.
 * E.g., Adding a sensor to a room that doesn't exist.
 */
public class LinkedResourceNotFoundException extends RuntimeException {
    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}
