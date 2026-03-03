package com.example.management.domain.exception;

public class InvalidOrderStateException extends DomainException {

    public InvalidOrderStateException(String message) {
        super(message);
    }
}

