package com.example.management.domain.exception;

public class InvalidItemException extends RuntimeException {

    public InvalidItemException(String message) {
        super(message);
    }
}

