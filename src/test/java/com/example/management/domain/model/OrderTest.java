package com.example.management.domain.model;

import com.example.management.domain.exception.InvalidOrderStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Order Aggregate")
class OrderTest {

    private UUID orderId;
    private UUID customerId;
    private LocalDateTime createdAt;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        createdAt = LocalDateTime.now();
    }

    @Nested
    @DisplayName("creation")
    class Creation {
        @Test
        void createsWithAtLeastOneItemAndCalculatesTotal() {
            OrderItem item1 = new OrderItem(UUID.randomUUID(), 2, new Money(new BigDecimal("10.00")));
            OrderItem item2 = new OrderItem(UUID.randomUUID(), 1, new Money(new BigDecimal("5.00")));
            Order order = new Order(orderId, customerId, createdAt, List.of(item1, item2));
            assertThat(order.getId()).isEqualTo(orderId);
            assertThat(order.getCustomerId()).isEqualTo(customerId);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(order.getItems()).hasSize(2);
            assertThat(order.getTotalAmount().getAmount()).isEqualByComparingTo("25.00");
        }

        @Test
        void cannotCreateWithEmptyItems() {
            assertThatThrownBy(() -> new Order(orderId, customerId, createdAt, List.of()))
                    .hasMessageContaining("at least one");
        }

        @Test
        void cannotCreateWithNullItems() {
            assertThatThrownBy(() -> new Order(orderId, customerId, createdAt, null))
                    .hasMessageContaining("at least one");
        }
    }

    @Nested
    @DisplayName("minimum order value")
    class MinimumOrderValue {
        @Test
        void cannotMarkAsPaidWhenTotalLessThan10Usd() {
            OrderItem item = new OrderItem(UUID.randomUUID(), 1, new Money(new BigDecimal("5.00")));
            Order order = new Order(orderId, customerId, createdAt, List.of(item));
            assertThatThrownBy(order::markAsPaid)
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("10");
        }

        @Test
        void canMarkAsPaidWhenTotalAtLeast10Usd() {
            OrderItem item = new OrderItem(UUID.randomUUID(), 1, new Money(new BigDecimal("10.00")));
            Order order = new Order(orderId, customerId, createdAt, List.of(item));
            order.markAsPaid();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        }
    }

    @Nested
    @DisplayName("state transitions")
    class StateTransitions {
        @Test
        void canCancelWhenPending() {
            OrderItem item = new OrderItem(UUID.randomUUID(), 1, new Money(new BigDecimal("15.00")));
            Order order = new Order(orderId, customerId, createdAt, List.of(item));
            order.cancel();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }

        @Test
        void canCancelWhenPaid() {
            OrderItem item = new OrderItem(UUID.randomUUID(), 1, new Money(new BigDecimal("15.00")));
            Order order = new Order(orderId, customerId, createdAt, List.of(item));
            order.markAsPaid();
            order.cancel();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }

        @Test
        void cannotCancelWhenShipped() {
            OrderItem item = new OrderItem(UUID.randomUUID(), 1, new Money(new BigDecimal("15.00")));
            Order order = new Order(orderId, customerId, createdAt, List.of(item));
            order.markAsPaid();
            order.markAsShipped();
            assertThatThrownBy(order::cancel)
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("cancel");
        }

        @Test
        void canShipOnlyWhenPaid() {
            OrderItem item = new OrderItem(UUID.randomUUID(), 1, new Money(new BigDecimal("15.00")));
            Order order = new Order(orderId, customerId, createdAt, List.of(item));
            assertThatThrownBy(order::markAsShipped)
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("SHIPPED");
            order.markAsPaid();
            order.markAsShipped();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        }
    }
}
