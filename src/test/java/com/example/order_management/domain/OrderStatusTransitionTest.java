package com.example.order_management.domain;

import com.example.order_management.domain.valueobject.Address;
import com.example.order_management.domain.valueobject.Money;
import com.example.order_management.domain.valueobject.OrderId;
import com.example.order_management.domain.exception.InvalidOrderStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TDD: State transitions for OrderStatus.
 * CANCELLED only from PENDING or PAID; SHIPPED only from PAID.
 */
@DisplayName("Order status transitions")
class OrderStatusTransitionTest {

    @Nested
    @DisplayName("CANCELLED")
    class Cancelled {

        @Test
        @DisplayName("allowed from PENDING")
        void allowedFromPending() {
            Order order = OrderFactory.create(OrderId.generate(), List.of(
                    OrderItem.create("SKU-1", Money.of(new BigDecimal("15.00"), "USD"), 1)
            ));
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);

            assertThatCode(() -> order.cancel()).doesNotThrowAnyException();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("allowed from PAID")
        void allowedFromPaid() {
            Order order = createPaidOrder();
            assertThatCode(() -> order.cancel()).doesNotThrowAnyException();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("not allowed from SHIPPED")
        void notAllowedFromShipped() {
            Order order = createShippedOrder();

            assertThatThrownBy(() -> order.cancel())
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("CANCELLED")
                    .hasMessageContaining("SHIPPED");
        }
    }

    @Nested
    @DisplayName("SHIPPED")
    class Shipped {

        @Test
        @DisplayName("allowed only from PAID")
        void allowedFromPaid() {
            Order order = createPaidOrder();
            assertThatCode(() -> order.ship(Address.of("Street", "City", "ZIP", "Country")))
                    .doesNotThrowAnyException();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        }

        @Test
        @DisplayName("not allowed from PENDING")
        void notAllowedFromPending() {
            Order order = OrderFactory.create(OrderId.generate(), List.of(
                    OrderItem.create("SKU-1", Money.of(new BigDecimal("15.00"), "USD"), 1)
            ));

            assertThatThrownBy(() -> order.ship(Address.of("S", "C", "Z", "CO")))
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("SHIPPED")
                    .hasMessageContaining("PENDING");
        }

        @Test
        @DisplayName("not allowed from CANCELLED")
        void notAllowedFromCancelled() {
            Order order = OrderFactory.create(OrderId.generate(), List.of(
                    OrderItem.create("SKU-1", Money.of(new BigDecimal("15.00"), "USD"), 1)
            ));
            order.cancel();

            assertThatThrownBy(() -> order.ship(Address.of("S", "C", "Z", "CO")))
                    .isInstanceOf(InvalidOrderStateException.class);
        }
    }

    private static Order createPaidOrder() {
        Order order = OrderFactory.create(OrderId.generate(), List.of(
                OrderItem.create("SKU-1", Money.of(new BigDecimal("15.00"), "USD"), 1)
        ));
        order.markAsPaid();
        return order;
    }

    private static Order createShippedOrder() {
        Order order = createPaidOrder();
        order.ship(Address.of("Street", "City", "ZIP", "Country"));
        return order;
    }
}
