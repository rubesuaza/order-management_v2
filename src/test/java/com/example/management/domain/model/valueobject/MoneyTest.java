package com.example.management.domain.model.valueobject;

import com.example.management.domain.exception.CurrencyMismatchException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoneyTest {

    @Test
    void shouldAddMoneyWithSameCurrency() {
        Money tenUsd = Money.of(new BigDecimal("10.00"), "USD");
        Money fiveUsd = Money.of(new BigDecimal("5.00"), "USD");

        Money result = tenUsd.add(fiveUsd);

        assertEquals(new BigDecimal("15.00"), result.getAmount());
        assertEquals("USD", result.getCurrency());
        // original instances must remain unchanged (immutability)
        assertEquals(new BigDecimal("10.00"), tenUsd.getAmount());
        assertEquals(new BigDecimal("5.00"), fiveUsd.getAmount());
    }

    @Test
    void shouldThrowCurrencyMismatchWhenAddingDifferentCurrencies() {
        Money tenUsd = Money.of(new BigDecimal("10.00"), "USD");
        Money fiveEur = Money.of(new BigDecimal("5.00"), "EUR");

        assertThrows(CurrencyMismatchException.class, () -> tenUsd.add(fiveEur));
    }

    @Test
    void shouldMultiplyMoneyAndRemainImmutable() {
        Money price = Money.of(new BigDecimal("2.50"), "USD");

        Money total = price.multiply(4);

        assertEquals(new BigDecimal("10.00"), total.getAmount());
        assertEquals("USD", total.getCurrency());
        // original instance must remain unchanged
        assertEquals(new BigDecimal("2.50"), price.getAmount());
    }
}

