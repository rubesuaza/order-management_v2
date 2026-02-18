package com.example.order_management.domain;

import com.example.order_management.domain.valueobject.Money;
import com.example.order_management.domain.valueobject.OrderId;
import com.example.order_management.domain.exception.InvalidOrderStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TDD: totalAmount is exactly the sum of (unitPrice * quantity) for all items.
 */
@DisplayName("Order total amount")
class OrderTotalAmountTest {

    @Test
    @DisplayName("total equals sum of line totals")
    void totalEqualsSumOfLineTotals() {
        OrderItem item1 = OrderItem.create("SKU-1", Money.of(new BigDecimal("10.00"), "USD"), 2);
        OrderItem item2 = OrderItem.create("SKU-2", Money.of(new BigDecimal("5.50"), "USD"), 3);

        Order order = OrderFactory.create(OrderId.generate(), List.of(item1, item2));

        // 10*2 + 5.50*3 = 20 + 16.50 = 36.50
        assertThat(order.getTotalAmount()).isEqualTo(Money.of(new BigDecimal("36.50"), "USD"));
    }

    @Test
    @DisplayName("single item total is unitPrice times quantity")
    void singleItemTotal() {
        Order order = OrderFactory.create(OrderId.generate(), List.of(
                OrderItem.create("SKU-1", Money.of(new BigDecimal("12.34"), "USD"), 4)
        ));
        assertThat(order.getTotalAmount()).isEqualTo(Money.of(new BigDecimal("49.36"), "USD"));
    }

    @Nested
    @DisplayName("invariants before PAID")
    class InvariantsBeforePaid {

        @Test
        @DisplayName("order must have at least one item")
        void orderMustHaveAtLeastOneItem() {
            assertThatThrownBy(() -> OrderFactory.create(OrderId.generate(), List.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("at least one");
        }

        @Test
        @DisplayName("cannot mark as PAID if total is less than 10.00 USD")
        void cannotMarkPaidIfTotalLessThanMinimum() {
            Order order = OrderFactory.create(OrderId.generate(), List.of(
                    OrderItem.create("SKU-1", Money.of(new BigDecimal("5.00"), "USD"), 1)
            ));
            assertThat(order.getTotalAmount().getAmount()).isLessThan(new BigDecimal("10.00"));

            assertThatThrownBy(order::markAsPaid)
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("10");
        }

        @Test
        @DisplayName("can mark as PAID when total is at least 10.00 USD")
        void canMarkPaidWhenTotalAtLeastMinimum() {
            Order order = OrderFactory.create(OrderId.generate(), List.of(
                    OrderItem.create("SKU-1", Money.of(new BigDecimal("10.00"), "USD"), 1)
            ));
            order.markAsPaid();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        }
    }
}
