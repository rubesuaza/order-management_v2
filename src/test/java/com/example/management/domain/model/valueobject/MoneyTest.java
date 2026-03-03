package com.example.management.domain.model.valueobject;

import com.example.management.domain.exception.CurrencyMismatchException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Test
    @DisplayName("add() debe sumar montos con la misma moneda")
    void addWithSameCurrencyReturnsSum() {
        Money tenUsd = Money.of(new BigDecimal("10.00"), "USD");
        Money fiveUsd = Money.of(new BigDecimal("5.00"), "USD");

        Money result = tenUsd.add(fiveUsd);

        assertEquals(new BigDecimal("15.00"), result.getAmount());
        assertEquals("USD", result.getCurrency());
    }

    @Test
    @DisplayName("add() debe lanzar CurrencyMismatchException si las monedas difieren")
    void addWithDifferentCurrencyThrowsException() {
        Money tenUsd = Money.of(new BigDecimal("10.00"), "USD");
        Money fiveEur = Money.of(new BigDecimal("5.00"), "EUR");

        assertThrows(CurrencyMismatchException.class, () -> tenUsd.add(fiveEur));
    }

    @Test
    @DisplayName("multiply() debe devolver una nueva instancia sin mutar el objeto original")
    void multiplyReturnsNewInstanceAndIsImmutable() {
        Money tenUsd = Money.of(new BigDecimal("10.00"), "USD");

        Money result = tenUsd.multiply(2);

        assertEquals(new BigDecimal("20.00"), result.getAmount());
        assertEquals("USD", result.getCurrency());

        // el original no cambia
        assertEquals(new BigDecimal("10.00"), tenUsd.getAmount());
    }

    @Test
    @DisplayName("Money.of() debe validar amount no nulo y currency no vacía")
    void factoryValidatesArguments() {
        assertThrows(IllegalArgumentException.class, () -> Money.of(null, "USD"));
        assertThrows(IllegalArgumentException.class, () -> Money.of(new BigDecimal("10.00"), null));
        assertThrows(IllegalArgumentException.class, () -> Money.of(new BigDecimal("10.00"), ""));
    }

    @Test
    @DisplayName("zero() debe crear un Money con monto cero y moneda indicada")
    void zeroFactoryCreatesZeroAmount() {
        Money zeroUsd = Money.zero("USD");

        assertEquals(BigDecimal.ZERO, zeroUsd.getAmount());
        assertEquals("USD", zeroUsd.getCurrency());
    }
}

