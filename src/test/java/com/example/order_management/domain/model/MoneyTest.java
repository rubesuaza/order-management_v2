package com.example.order_management.domain.model;

import com.example.order_management.domain.exception.CurrencyMismatchException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Money Value Object Tests")
class MoneyTest {

    @Test
    @DisplayName("Should create Money with valid amount and currency")
    void shouldCreateMoneyWithValidAmountAndCurrency() {
        // Given
        BigDecimal amount = new BigDecimal("100.50");
        Currency currency = Currency.getInstance("USD");

        // When
        Money money = new Money(amount, currency);

        // Then
        assertThat(money.getAmount()).isEqualByComparingTo(amount);
        assertThat(money.getCurrency()).isEqualTo(currency);
    }

    @Test
    @DisplayName("Should throw exception when adding Money with different currencies")
    void shouldThrowExceptionWhenAddingDifferentCurrencies() {
        // Given
        Money usdMoney = new Money(new BigDecimal("100.00"), Currency.getInstance("USD"));
        Money eurMoney = new Money(new BigDecimal("50.00"), Currency.getInstance("EUR"));

        // When & Then
        assertThatThrownBy(() -> usdMoney.add(eurMoney))
                .isInstanceOf(CurrencyMismatchException.class)
                .hasMessageContaining("Currency mismatch");
    }

    @Test
    @DisplayName("Should add Money with same currency correctly")
    void shouldAddMoneyWithSameCurrency() {
        // Given
        Money money1 = new Money(new BigDecimal("100.50"), Currency.getInstance("USD"));
        Money money2 = new Money(new BigDecimal("50.25"), Currency.getInstance("USD"));

        // When
        Money result = money1.add(money2);

        // Then
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("150.75"));
        assertThat(result.getCurrency()).isEqualTo(Currency.getInstance("USD"));
    }

    @Test
    @DisplayName("Should multiply Money correctly")
    void shouldMultiplyMoney() {
        // Given
        Money money = new Money(new BigDecimal("10.50"), Currency.getInstance("USD"));
        BigDecimal multiplier = new BigDecimal("3");

        // When
        Money result = money.multiply(multiplier);

        // Then
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("31.50"));
        assertThat(result.getCurrency()).isEqualTo(Currency.getInstance("USD"));
    }

    @Test
    @DisplayName("Should compare Money values correctly")
    void shouldCompareMoneyValues() {
        // Given
        Money money1 = new Money(new BigDecimal("100.00"), Currency.getInstance("USD"));
        Money money2 = new Money(new BigDecimal("100.00"), Currency.getInstance("USD"));
        Money money3 = new Money(new BigDecimal("50.00"), Currency.getInstance("USD"));

        // When & Then
        assertThat(money1).isEqualTo(money2);
        assertThat(money1.compareTo(money3)).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should throw exception when comparing Money with different currencies")
    void shouldThrowExceptionWhenComparingDifferentCurrencies() {
        // Given
        Money usdMoney = new Money(new BigDecimal("100.00"), Currency.getInstance("USD"));
        Money eurMoney = new Money(new BigDecimal("100.00"), Currency.getInstance("EUR"));

        // When & Then
        assertThatThrownBy(() -> usdMoney.compareTo(eurMoney))
                .isInstanceOf(CurrencyMismatchException.class);
    }
}
