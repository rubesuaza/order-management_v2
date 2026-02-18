package com.example.order_management.domain.exception;

/**
 * Thrown when an illegal state transition is attempted on an Order (e.g. CANCELLED from SHIPPED).
 */
public final class InvalidOrderStateException extends RuntimeException {

    public InvalidOrderStateException(String message) {
        super(message);
    }
}
