package com.example.management.order.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    @DisplayName("No debe permitir cantidad nula ni moneda nula o vacía")
    void constructorValidation() {
        assertThatThrownBy(() -> new Money(null, "USD"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new Money(BigDecimal.TEN, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new Money(BigDecimal.TEN, ""))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Las operaciones entre distintas monedas deben fallar con CurrencyMismatchException")
    void currencyMismatch() {
        Money usd = new Money(new BigDecimal("10.00"), "USD");
        Money eur = new Money(new BigDecimal("5.00"), "EUR");

        assertThatThrownBy(() -> usd.add(eur))
                .isInstanceOf(CurrencyMismatchException.class);

        assertThatThrownBy(() -> usd.subtract(eur))
                .isInstanceOf(CurrencyMismatchException.class);
    }

    @Test
    @DisplayName("Debe permitir sumar, restar y multiplicar correctamente con misma moneda")
    void operationsSameCurrency() {
        Money base = new Money(new BigDecimal("10.00"), "USD");
        Money other = new Money(new BigDecimal("5.50"), "USD");

        Money sum = base.add(other);
        assertThat(sum.amount()).isEqualByComparingTo(new BigDecimal("15.50"));
        assertThat(sum.currency()).isEqualTo("USD");

        Money diff = base.subtract(new Money(new BigDecimal("3.00"), "USD"));
        assertThat(diff.amount()).isEqualByComparingTo(new BigDecimal("7.00"));

        Money multiplied = base.multiply(2);
        assertThat(multiplied.amount()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(multiplied.currency()).isEqualTo("USD");
    }
}

