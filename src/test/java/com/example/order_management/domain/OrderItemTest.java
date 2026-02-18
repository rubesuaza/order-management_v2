package com.example.order_management.domain;

import com.example.order_management.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TDD: Tests for OrderItem invariants.
 * Invariants: quantity > 0, unitPrice cannot be negative.
 */
@DisplayName("OrderItem")
class OrderItemTest {

    @Nested
    @DisplayName("creation")
    class Creation {

        @Test
        @DisplayName("rejects negative unit price")
        void rejectsNegativeUnitPrice() {
            Money negativePrice = Money.of(new BigDecimal("-5.00"), "USD");

            assertThatThrownBy(() -> OrderItem.create("SKU-1", negativePrice, 2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("negative")
                    .hasMessageContaining("price");
        }

        @Test
        @DisplayName("rejects zero quantity")
        void rejectsZeroQuantity() {
            Money price = Money.of(new BigDecimal("10.00"), "USD");

            assertThatThrownBy(() -> OrderItem.create("SKU-1", price, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("quantity");
        }

        @Test
        @DisplayName("rejects negative quantity")
        void rejectsNegativeQuantity() {
            Money price = Money.of(new BigDecimal("10.00"), "USD");

            assertThatThrownBy(() -> OrderItem.create("SKU-1", price, -1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("quantity");
        }

        @Test
        @DisplayName("creates with valid productId, unitPrice and quantity")
        void createsWithValidData() {
            Money price = Money.of(new BigDecimal("10.00"), "USD");
            OrderItem item = OrderItem.create("SKU-1", price, 2);

            assertThat(item.getProductId()).isEqualTo("SKU-1");
            assertThat(item.getUnitPrice()).isEqualTo(price);
            assertThat(item.getQuantity()).isEqualTo(2);
        }

        @Test
        @DisplayName("line total is unitPrice times quantity")
        void lineTotalIsUnitPriceTimesQuantity() {
            Money price = Money.of(new BigDecimal("10.50"), "USD");
            OrderItem item = OrderItem.create("SKU-1", price, 3);

            assertThat(item.getLineTotal()).isEqualTo(Money.of(new BigDecimal("31.50"), "USD"));
        }
    }
}
