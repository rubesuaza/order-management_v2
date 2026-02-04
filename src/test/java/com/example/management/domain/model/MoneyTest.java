package com.example.management.domain.model;

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
        void createsWithAmountAndDefaultCurrency() {
            Money money = new Money(new BigDecimal("99.99"));
            assertThat(money.getAmount()).isEqualByComparingTo("99.99");
            assertThat(money.getCurrency()).isEqualTo("USD");
        }

        @Test
        void createsWithAmountAndCurrency() {
            Money money = new Money(new BigDecimal("50.00"), "EUR");
            assertThat(money.getAmount()).isEqualByComparingTo("50.00");
            assertThat(money.getCurrency()).isEqualTo("EUR");
        }
    }

    @Nested
    @DisplayName("addition")
    class Addition {
        @Test
        void addsSameCurrency() {
            Money a = new Money(new BigDecimal("10.00"));
            Money b = new Money(new BigDecimal("5.50"));
            Money sum = a.add(b);
            assertThat(sum.getAmount()).isEqualByComparingTo("15.50");
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
            Money a = new Money(new BigDecimal("20.00"));
            Money b = new Money(new BigDecimal("7.25"));
            Money result = a.subtract(b);
            assertThat(result.getAmount()).isEqualByComparingTo("12.75");
        }

        @Test
        void subtractThrowsWhenCurrenciesDiffer() {
            Money usd = new Money(new BigDecimal("20.00"), "USD");
            Money eur = new Money(new BigDecimal("5.00"), "EUR");
            assertThatThrownBy(() -> usd.subtract(eur))
                    .isInstanceOf(CurrencyMismatchException.class);
        }
    }

    @Nested
    @DisplayName("multiplication")
    class Multiplication {
        @Test
        void multipliesByScalar() {
            Money money = new Money(new BigDecimal("10.00"));
            Money result = money.multiply(3);
            assertThat(result.getAmount()).isEqualByComparingTo("30.00");
            assertThat(result.getCurrency()).isEqualTo("USD");
        }

        @Test
        void multiplyPreservesCurrency() {
            Money money = new Money(new BigDecimal("2.50"), "EUR");
            Money result = money.multiply(2);
            assertThat(result.getCurrency()).isEqualTo("EUR");
        }
    }
}
