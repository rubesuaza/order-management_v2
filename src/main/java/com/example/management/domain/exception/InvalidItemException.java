package com.example.management.domain.exception;

/**
 * Thrown when an order item violates business rules
 * (e.g. quantity <= 0 or negative unit price).
 */
public class InvalidItemException extends DomainException {

    public InvalidItemException(String message) {
        super(message);
    }
}
