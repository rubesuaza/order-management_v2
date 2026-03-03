package com.example.management.order.domain.model.valueobject;

import com.example.management.order.domain.exception.CurrencyMismatchException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoneyTest {

    @Test
    void addWithSameCurrencyReturnsNewInstanceWithSummedAmount() {
        Money tenUsd = Money.of(new BigDecimal("10.00"), "USD");
        Money fiveUsd = Money.of(new BigDecimal("5.00"), "USD");

        Money result = tenUsd.add(fiveUsd);

        assertEquals(new BigDecimal("15.00"), result.amount());
        assertEquals("USD", result.currency());
        assertNotSame(tenUsd, result);
        assertNotSame(fiveUsd, result);
    }

    @Test
    void addWithDifferentCurrencyThrowsCurrencyMismatchException() {
        Money tenUsd = Money.of(new BigDecimal("10.00"), "USD");
        Money fiveEur = Money.of(new BigDecimal("5.00"), "EUR");

        assertThrows(CurrencyMismatchException.class, () -> tenUsd.add(fiveEur));
    }

    @Test
    void multiplyReturnsNewInstanceAndDoesNotMutateOriginal() {
        Money tenUsd = Money.of(new BigDecimal("10.00"), "USD");

        Money result = tenUsd.multiply(new BigDecimal("2"));

        assertEquals(new BigDecimal("20.00"), result.amount());
        assertEquals("USD", result.currency());
        assertEquals(new BigDecimal("10.00"), tenUsd.amount());
    }
}

