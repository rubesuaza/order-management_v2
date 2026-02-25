package com.example.management.domain.exception;

/**
 * Base exception for all domain errors.
 * Domain layer has zero dependencies on external frameworks.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
