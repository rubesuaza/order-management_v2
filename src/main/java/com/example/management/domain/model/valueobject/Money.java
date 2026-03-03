package com.example.management.domain.model.valueobject;

import com.example.management.domain.exception.CurrencyMismatchException;

import java.math.BigDecimal;
import java.util.Objects;

public final class Money {

    private final BigDecimal amount;
    private final String currency;

    private Money(BigDecimal amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public static Money of(BigDecimal amount, String currency) {
        if (amount == null) {
            throw new IllegalArgumentException("amount must not be null");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("currency must not be null or empty");
        }
        return new Money(amount, currency);
    }

    public static Money zero(String currency) {
        return of(BigDecimal.ZERO, currency);
    }

    public Money add(Money other) {
        assertSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        assertSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    public Money multiply(int factor) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(factor)), this.currency);
    }

    private void assertSameCurrency(Money other) {
        if (other == null) {
            throw new IllegalArgumentException("other must not be null");
        }
        if (!Objects.equals(this.currency, other.currency)) {
            throw new CurrencyMismatchException("Cannot operate on Money with different currencies");
        }
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return amount.compareTo(money.amount) == 0 &&
                Objects.equals(currency, money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros(), currency);
    }

    @Override
    public String toString() {
        return amount + " " + currency;
    }
}

