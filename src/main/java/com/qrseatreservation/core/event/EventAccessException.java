package com.qrseatreservation.core.event;

/**
 * Raised when an operation is not allowed for the current event: writes to an archived event,
 * check-in on a non-active event, access to data of another event, or no event being available.
 */
public class EventAccessException extends RuntimeException {
    public EventAccessException(String message) {
        super(message);
    }
}
