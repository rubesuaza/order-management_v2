package com.example.management.domain.exception;

/**
 * Thrown when a monetary operation involves different currencies.
 */
public class CurrencyMismatchException extends DomainException {

    public CurrencyMismatchException(String message) {
        super(message);
    }
}
