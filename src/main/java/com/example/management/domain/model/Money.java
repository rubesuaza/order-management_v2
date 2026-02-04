package com.example.management.domain.model;

import com.example.management.domain.exception.CurrencyMismatchException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value object representing a monetary amount. Immutable.
 * Supports addition, subtraction, and multiplication. All operations require same currency.
 */
public final class Money {

    private static final String DEFAULT_CURRENCY = "USD";
    private static final int SCALE = 2;

    private final BigDecimal amount;
    private final String currency;

    public Money(BigDecimal amount) {
        this(amount, DEFAULT_CURRENCY);
    }

    public Money(BigDecimal amount, String currency) {
        this.amount = amount == null ? BigDecimal.ZERO : amount.setScale(SCALE, RoundingMode.HALF_UP);
        this.currency = currency == null || currency.isBlank() ? DEFAULT_CURRENCY : currency.trim();
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        requireSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    public Money multiply(int factor) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(factor)), this.currency);
    }

    private void requireSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new CurrencyMismatchException(
                    "Currency mismatch: cannot operate on different currency (" + this.currency + " vs " + other.currency + ")");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return amount.compareTo(money.amount) == 0 && Objects.equals(currency, money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }
}
