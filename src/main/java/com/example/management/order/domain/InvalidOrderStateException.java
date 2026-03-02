package com.example.management.order.domain;

public class InvalidOrderStateException extends DomainException {

    public InvalidOrderStateException(String message) {
        super(message);
    }
}

