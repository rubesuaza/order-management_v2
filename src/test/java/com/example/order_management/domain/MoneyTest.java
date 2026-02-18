package com.example.order_management.domain;

import com.example.order_management.domain.valueobject.Money;
import com.example.order_management.domain.exception.CurrencyMismatchException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TDD: Money value object - currency consistency in add/multiply.
 */
@DisplayName("Money")
class MoneyTest {

    @Nested
    @DisplayName("addition")
    class Addition {

        @Test
        @DisplayName("adds same currency")
        void addsSameCurrency() {
            Money a = Money.of(new BigDecimal("10.00"), "USD");
            Money b = Money.of(new BigDecimal("5.50"), "USD");
            assertThat(a.add(b)).isEqualTo(Money.of(new BigDecimal("15.50"), "USD"));
        }

        @Test
        @DisplayName("throws CurrencyMismatchException when currencies differ")
        void throwsWhenCurrenciesDiffer() {
            Money usd = Money.of(new BigDecimal("10.00"), "USD");
            Money eur = Money.of(new BigDecimal("5.00"), "EUR");

            assertThatThrownBy(() -> usd.add(eur))
                    .isInstanceOf(CurrencyMismatchException.class)
                    .hasMessageContaining("USD")
                    .hasMessageContaining("EUR");
        }
    }

    @Nested
    @DisplayName("multiplication")
    class Multiplication {

        @Test
        @DisplayName("multiplies amount and keeps currency")
        void multipliesAmountKeepsCurrency() {
            Money m = Money.of(new BigDecimal("10.00"), "USD");
            assertThat(m.multiply(3)).isEqualTo(Money.of(new BigDecimal("30.00"), "USD"));
        }

        @Test
        @DisplayName("multiply by decimal factor")
        void multiplyByDecimalFactor() {
            Money m = Money.of(new BigDecimal("10.00"), "USD");
            assertThat(m.multiply(new BigDecimal("2.5"))).isEqualTo(Money.of(new BigDecimal("25.00"), "USD"));
        }
    }

    @Test
    @DisplayName("equals and hashCode by value and currency")
    void equalsAndHashCode() {
        Money a = Money.of(new BigDecimal("10.00"), "USD");
        Money b = Money.of(new BigDecimal("10.00"), "USD");
        Money c = Money.of(new BigDecimal("10.00"), "EUR");
        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(c);
    }
}
