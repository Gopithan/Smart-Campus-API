package com.mycompany.smart.campus.api.exceptions;

/**
 * Thrown when an attempt is made to delete a room that still contains sensors.
 */
public class RoomNotEmptyException extends RuntimeException {
    public RoomNotEmptyException(String message) {
        super(message);
    }
}
