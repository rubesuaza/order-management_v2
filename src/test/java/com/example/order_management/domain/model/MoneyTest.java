package com.example.order_management.domain.model;

import com.example.order_management.domain.exception.CurrencyMismatchException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Money Value Object Tests")
class MoneyTest {

    @Test
    @DisplayName("Should create Money with default currency USD")
    void shouldCreateMoneyWithDefaultCurrency() {
        Money money = new Money(new BigDecimal("100.00"));
        
        assertThat(money.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(money.getCurrency()).isEqualTo("USD");
    }

    @Test
    @DisplayName("Should create Money with specified currency")
    void shouldCreateMoneyWithSpecifiedCurrency() {
        Money money = new Money(new BigDecimal("50.00"), "EUR");
        
        assertThat(money.getAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(money.getCurrency()).isEqualTo("EUR");
    }

    @Test
    @DisplayName("Should add two Money objects with same currency")
    void shouldAddMoneyWithSameCurrency() {
        Money money1 = new Money(new BigDecimal("100.00"), "USD");
        Money money2 = new Money(new BigDecimal("50.00"), "USD");
        
        Money result = money1.add(money2);
        
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(result.getCurrency()).isEqualTo("USD");
    }

    @Test
    @DisplayName("Should throw CurrencyMismatchException when adding Money with different currencies")
    void shouldThrowExceptionWhenAddingDifferentCurrencies() {
        Money money1 = new Money(new BigDecimal("100.00"), "USD");
        Money money2 = new Money(new BigDecimal("50.00"), "EUR");
        
        assertThatThrownBy(() -> money1.add(money2))
                .isInstanceOf(CurrencyMismatchException.class)
                .hasMessageContaining("currency");
    }

    @Test
    @DisplayName("Should subtract two Money objects with same currency")
    void shouldSubtractMoneyWithSameCurrency() {
        Money money1 = new Money(new BigDecimal("100.00"), "USD");
        Money money2 = new Money(new BigDecimal("30.00"), "USD");
        
        Money result = money1.subtract(money2);
        
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("70.00"));
        assertThat(result.getCurrency()).isEqualTo("USD");
    }

    @Test
    @DisplayName("Should throw CurrencyMismatchException when subtracting Money with different currencies")
    void shouldThrowExceptionWhenSubtractingDifferentCurrencies() {
        Money money1 = new Money(new BigDecimal("100.00"), "USD");
        Money money2 = new Money(new BigDecimal("30.00"), "EUR");
        
        assertThatThrownBy(() -> money1.subtract(money2))
                .isInstanceOf(CurrencyMismatchException.class)
                .hasMessageContaining("currency");
    }

    @Test
    @DisplayName("Should multiply Money by a factor")
    void shouldMultiplyMoneyByFactor() {
        Money money = new Money(new BigDecimal("25.00"), "USD");
        
        Money result = money.multiply(3);
        
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("75.00"));
        assertThat(result.getCurrency()).isEqualTo("USD");
    }

    @Test
    @DisplayName("Should multiply Money by BigDecimal factor")
    void shouldMultiplyMoneyByBigDecimalFactor() {
        Money money = new Money(new BigDecimal("25.00"), "USD");
        
        Money result = money.multiply(new BigDecimal("2.5"));
        
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("62.50"));
        assertThat(result.getCurrency()).isEqualTo("USD");
    }

    @Test
    @DisplayName("Should be equal when amount and currency are the same")
    void shouldBeEqualWhenAmountAndCurrencyAreSame() {
        Money money1 = new Money(new BigDecimal("100.00"), "USD");
        Money money2 = new Money(new BigDecimal("100.00"), "USD");
        
        assertThat(money1).isEqualTo(money2);
        assertThat(money1.hashCode()).isEqualTo(money2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when currencies differ")
    void shouldNotBeEqualWhenCurrenciesDiffer() {
        Money money1 = new Money(new BigDecimal("100.00"), "USD");
        Money money2 = new Money(new BigDecimal("100.00"), "EUR");
        
        assertThat(money1).isNotEqualTo(money2);
    }

    @Test
    @DisplayName("Should not be equal when amounts differ")
    void shouldNotBeEqualWhenAmountsDiffer() {
        Money money1 = new Money(new BigDecimal("100.00"), "USD");
        Money money2 = new Money(new BigDecimal("200.00"), "USD");
        
        assertThat(money1).isNotEqualTo(money2);
    }
}
