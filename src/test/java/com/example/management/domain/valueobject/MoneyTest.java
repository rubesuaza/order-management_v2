package com.example.management.domain.valueobject;

import com.example.management.domain.exception.CurrencyMismatchException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Money Value Object")
class MoneyTest {

    @Nested
    @DisplayName("creation")
    class Creation {
        @Test
        void createsWithAmountAndDefaultCurrencyUsd() {
            Money m = new Money(new BigDecimal("10.00"));
            assertThat(m.getAmount()).isEqualByComparingTo(new BigDecimal("10.00"));
            assertThat(m.getCurrency()).isEqualTo("USD");
        }

        @Test
        void createsWithAmountAndExplicitCurrency() {
            Money m = new Money(new BigDecimal("5.50"), "EUR");
            assertThat(m.getAmount()).isEqualByComparingTo(new BigDecimal("5.50"));
            assertThat(m.getCurrency()).isEqualTo("EUR");
        }

        @Test
        void rejectsNegativeAmount() {
            assertThatThrownBy(() -> new Money(new BigDecimal("-1.00")))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("addition")
    class Addition {
        @Test
        void addsSameCurrency() {
            Money a = new Money(new BigDecimal("10.00"), "USD");
            Money b = new Money(new BigDecimal("5.00"), "USD");
            Money sum = a.add(b);
            assertThat(sum.getAmount()).isEqualByComparingTo(new BigDecimal("15.00"));
            assertThat(sum.getCurrency()).isEqualTo("USD");
        }

        @Test
        void addThrowsWhenCurrenciesDiffer() {
            Money usd = new Money(new BigDecimal("10.00"), "USD");
            Money eur = new Money(new BigDecimal("5.00"), "EUR");
            assertThatThrownBy(() -> usd.add(eur))
                    .isInstanceOf(CurrencyMismatchException.class)
                    .hasMessageContaining("currency");
        }
    }

    @Nested
    @DisplayName("subtraction")
    class Subtraction {
        @Test
        void subtractsSameCurrency() {
            Money a = new Money(new BigDecimal("10.00"), "USD");
            Money b = new Money(new BigDecimal("3.00"), "USD");
            Money diff = a.subtract(b);
            assertThat(diff.getAmount()).isEqualByComparingTo(new BigDecimal("7.00"));
        }

        @Test
        void subtractThrowsWhenCurrenciesDiffer() {
            Money usd = new Money(new BigDecimal("10.00"), "USD");
            Money eur = new Money(new BigDecimal("5.00"), "EUR");
            assertThatThrownBy(() -> usd.subtract(eur))
                    .isInstanceOf(CurrencyMismatchException.class);
        }
    }

    @Nested
    @DisplayName("multiplication")
    class Multiplication {
        @Test
        void multipliesByPositiveScalar() {
            Money m = new Money(new BigDecimal("10.00"), "USD");
            Money result = m.multiply(3);
            assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("30.00"));
            assertThat(result.getCurrency()).isEqualTo("USD");
        }

        @Test
        void multiplyByZero() {
            Money m = new Money(new BigDecimal("10.00"), "USD");
            Money result = m.multiply(0);
            assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("equality")
    class Equality {
        @Test
        void equalsByAmountAndCurrency() {
            Money a = new Money(new BigDecimal("10.00"), "USD");
            Money b = new Money(new BigDecimal("10.00"), "USD");
            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        void notEqualsWhenAmountDiffers() {
            Money a = new Money(new BigDecimal("10.00"), "USD");
            Money b = new Money(new BigDecimal("10.01"), "USD");
            assertThat(a).isNotEqualTo(b);
        }

        @Test
        void notEqualsWhenCurrencyDiffers() {
            Money a = new Money(new BigDecimal("10.00"), "USD");
            Money b = new Money(new BigDecimal("10.00"), "EUR");
            assertThat(a).isNotEqualTo(b);
        }
    }
}
