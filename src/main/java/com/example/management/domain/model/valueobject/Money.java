package com.example.management.domain.model.valueobject;

import com.example.management.domain.exception.CurrencyMismatchException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object que representa una cantidad de dinero en una moneda concreta.
 */
public record Money(BigDecimal amount, String currency) {

    private static final int SCALE = 2;

    public Money {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
    }

    public static Money of(BigDecimal amount, String currency) {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        BigDecimal scaled = amount.setScale(SCALE, RoundingMode.HALF_UP);
        return new Money(scaled, currency);
    }

    public Money add(Money other) {
        Objects.requireNonNull(other, "other must not be null");
        if (!currency.equals(other.currency)) {
            throw new CurrencyMismatchException(
                    "Cannot add Money with different currencies: %s and %s"
                            .formatted(currency, other.currency));
        }
        BigDecimal result = amount.add(other.amount)
                .setScale(SCALE, RoundingMode.HALF_UP);
        return new Money(result, currency);
    }

    public Money multiply(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        BigDecimal result = amount
                .multiply(BigDecimal.valueOf(quantity))
                .setScale(SCALE, RoundingMode.HALF_UP);
        return new Money(result, currency);
    }
}

