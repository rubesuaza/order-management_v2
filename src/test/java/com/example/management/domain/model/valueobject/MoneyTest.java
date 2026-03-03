package com.example.management.domain.model.valueobject;

import com.example.management.domain.exception.CurrencyMismatchException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Test
    void addShouldSumAmountsWithSameCurrency() {
        Money first = Money.of(new BigDecimal("10.00"), "USD");
        Money second = Money.of(new BigDecimal("5.50"), "USD");

        Money result = first.add(second);

        assertEquals(new BigDecimal("15.50"), result.amount());
        assertEquals("USD", result.currency());

        // immutability
        assertEquals(new BigDecimal("10.00"), first.amount());
        assertEquals(new BigDecimal("5.50"), second.amount());
    }

    @Test
    void addShouldThrowWhenCurrenciesDiffer() {
        Money usd = Money.of(new BigDecimal("10.00"), "USD");
        Money eur = Money.of(new BigDecimal("5.00"), "EUR");

        assertThrows(CurrencyMismatchException.class, () -> usd.add(eur));
    }

    @Test
    void multiplyShouldReturnNewInstance() {
        Money base = Money.of(new BigDecimal("10.00"), "USD");

        Money result = base.multiply(2);

        assertEquals(new BigDecimal("20.00"), result.amount());
        assertEquals("USD", result.currency());
        assertNotSame(base, result);
    }

    @Test
    void factoryShouldValidateArguments() {
        assertThrows(IllegalArgumentException.class, () -> Money.of(null, "USD"));
        assertThrows(IllegalArgumentException.class, () -> Money.of(BigDecimal.TEN, null));
        assertThrows(IllegalArgumentException.class, () -> Money.of(BigDecimal.TEN, "  "));
    }
}

