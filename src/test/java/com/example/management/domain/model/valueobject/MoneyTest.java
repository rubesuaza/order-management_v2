package com.example.management.domain.model.valueobject;

import com.example.management.domain.exception.CurrencyMismatchException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Test
    @DisplayName("Dos Money con misma moneda se pueden sumar y devuelven nuevo objeto inmutable")
    void addWithSameCurrencyReturnsNewInstance() {
        Money m1 = Money.of(new BigDecimal("10.00"), "USD");
        Money m2 = Money.of(new BigDecimal("5.50"), "USD");

        Money result = m1.add(m2);

        assertEquals(new BigDecimal("15.50"), result.amount());
        assertEquals("USD", result.currency());
        assertNotSame(m1, result);
        assertNotSame(m2, result);
    }

    @Test
    @DisplayName("Suma de Money con distinta moneda lanza CurrencyMismatchException")
    void addWithDifferentCurrencyThrowsException() {
        Money usd = Money.of(new BigDecimal("10.00"), "USD");
        Money eur = Money.of(new BigDecimal("5.00"), "EUR");

        assertThrows(CurrencyMismatchException.class, () -> usd.add(eur));
    }

    @Test
    @DisplayName("Multiplicar Money por cantidad positiva devuelve nuevo Money con importe correcto")
    void multiplyWithPositiveQuantity() {
        Money price = Money.of(new BigDecimal("2.50"), "USD");

        Money total = price.multiply(4);

        assertEquals(new BigDecimal("10.00"), total.amount());
        assertEquals("USD", total.currency());
    }

    @Test
    @DisplayName("Multiplicar Money por cantidad no positiva lanza IllegalArgumentException")
    void multiplyWithNonPositiveQuantityThrowsException() {
        Money price = Money.of(new BigDecimal("2.50"), "USD");

        assertThrows(IllegalArgumentException.class, () -> price.multiply(0));
        assertThrows(IllegalArgumentException.class, () -> price.multiply(-1));
    }
}

