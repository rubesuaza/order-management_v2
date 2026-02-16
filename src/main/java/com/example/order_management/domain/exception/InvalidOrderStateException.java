package com.example.order_management.domain.exception;

/**
 * Exception thrown when an invalid state transition is attempted on an Order.
 */
public class InvalidOrderStateException extends DomainException {
    
    public InvalidOrderStateException(String message) {
        super(message);
    }
}
