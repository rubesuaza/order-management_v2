package com.example.management.domain.exception;

/**
 * Thrown when an illegal order state transition is attempted
 * (e.g. cancelling a SHIPPED order, or shipping a PENDING order).
 */
public class InvalidOrderStateException extends DomainException {

    public InvalidOrderStateException(String message) {
        super(message);
    }
}
