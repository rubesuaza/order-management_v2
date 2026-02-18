package com.example.order_management.domain.exception;

public class CurrencyMismatchException extends RuntimeException {
    
    public CurrencyMismatchException(String message) {
        super(message);
    }
}
