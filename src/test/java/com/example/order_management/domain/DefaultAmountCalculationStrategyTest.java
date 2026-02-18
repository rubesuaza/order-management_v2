package com.example.order_management.domain;

import com.example.order_management.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for DefaultAmountCalculationStrategy. Sum of line totals.
 */
@DisplayName("DefaultAmountCalculationStrategy")
class DefaultAmountCalculationStrategyTest {

    private DefaultAmountCalculationStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new DefaultAmountCalculationStrategy();
    }

    @Test
    @DisplayName("single item returns its line total")
    void singleItemReturnsLineTotal() {
        List<OrderItem> items = List.of(
                OrderItem.create("SKU-1", Money.of(new BigDecimal("10.00"), "USD"), 2)
        );
        Money total = strategy.calculateTotal(items);
        assertThat(total).isEqualTo(Money.of(new BigDecimal("20.00"), "USD"));
    }

    @Test
    @DisplayName("multiple items returns sum of line totals")
    void multipleItemsReturnsSumOfLineTotals() {
        List<OrderItem> items = List.of(
                OrderItem.create("SKU-1", Money.of(new BigDecimal("10.00"), "USD"), 2),
                OrderItem.create("SKU-2", Money.of(new BigDecimal("5.50"), "USD"), 3)
        );
        Money total = strategy.calculateTotal(items);
        assertThat(total).isEqualTo(Money.of(new BigDecimal("36.50"), "USD"));
    }

    @Nested
    @DisplayName("invalid input")
    class InvalidInput {

        @Test
        @DisplayName("throws when items is null")
        void throwsWhenItemsIsNull() {
            assertThatThrownBy(() -> strategy.calculateTotal(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("At least one item");
        }

        @Test
        @DisplayName("throws when items is empty")
        void throwsWhenItemsIsEmpty() {
            assertThatThrownBy(() -> strategy.calculateTotal(List.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("At least one item");
        }
    }
}
