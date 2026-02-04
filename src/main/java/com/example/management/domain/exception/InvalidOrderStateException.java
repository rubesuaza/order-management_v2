package com.example.management.domain.exception;

/**
 * Thrown when an illegal order state transition is attempted.
 */
public class InvalidOrderStateException extends DomainException {

    public InvalidOrderStateException(String message) {
        super(message);
    }
}
