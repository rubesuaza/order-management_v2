package com.example.order_management.domain.exception;

public class InvalidOrderStateException extends RuntimeException {
    
    public InvalidOrderStateException(String message) {
        super(message);
    }
}
