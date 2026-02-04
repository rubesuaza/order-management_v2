package com.example.management.domain.exception;

/**
 * Thrown when an order item has invalid quantity or price.
 */
public class InvalidItemException extends DomainException {

    public InvalidItemException(String message) {
        super(message);
    }
}
