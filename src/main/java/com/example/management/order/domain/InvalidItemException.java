package com.example.management.order.domain;

public class InvalidItemException extends DomainException {

    public InvalidItemException(String message) {
        super(message);
    }
}

