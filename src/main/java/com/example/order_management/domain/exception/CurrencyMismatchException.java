package com.example.order_management.domain.exception;

/**
 * Thrown when a monetary operation involves inconsistent currencies (e.g. USD + EUR).
 */
public final class CurrencyMismatchException extends RuntimeException {

    public CurrencyMismatchException(String message) {
        super(message);
    }

    public CurrencyMismatchException(String currency1, String currency2) {
        super("Currency mismatch: cannot operate between " + currency1 + " and " + currency2);
    }
}
