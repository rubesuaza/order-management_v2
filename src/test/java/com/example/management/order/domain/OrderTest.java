package com.example.management.order.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    @Test
    @DisplayName("Un pedido debe tener al menos un item")
    void orderMustHaveAtLeastOneItem() {
        UUID customerId = UUID.randomUUID();

        assertThatThrownBy(() -> Order.createNew(customerId, List.of()))
                .isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    @DisplayName("El total del pedido es la suma de (precio * cantidad) de todos los items")
    void totalAmountCalculation() {
        UUID customerId = UUID.randomUUID();

        OrderItem item1 = new OrderItem(UUID.randomUUID(), 2, new Money(new BigDecimal("5.00"), "USD"));
        OrderItem item2 = new OrderItem(UUID.randomUUID(), 1, new Money(new BigDecimal("10.00"), "USD"));

        Order order = Order.createNew(customerId, List.of(item1, item2));

        assertThat(order.totalAmount().amount()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(order.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.createdAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("No se puede marcar como pagado si el total es menor que 10.00 USD")
    void cannotPayIfTotalBelowMinimum() {
        UUID customerId = UUID.randomUUID();

        OrderItem cheapItem = new OrderItem(UUID.randomUUID(), 1, new Money(new BigDecimal("5.00"), "USD"));
        Order order = Order.createNew(customerId, List.of(cheapItem));

        assertThatThrownBy(order::markAsPaid)
                .isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    @DisplayName("Debe permitir transición PENDING -> PAID -> SHIPPED -> DELIVERED")
    void validStateTransitions() {
        UUID customerId = UUID.randomUUID();

        OrderItem item = new OrderItem(UUID.randomUUID(), 2, new Money(new BigDecimal("10.00"), "USD"));
        Order order = Order.createNew(customerId, List.of(item));

        order.markAsPaid();
        assertThat(order.status()).isEqualTo(OrderStatus.PAID);

        order.markAsShipped();
        assertThat(order.status()).isEqualTo(OrderStatus.SHIPPED);

        order.markAsDelivered();
        assertThat(order.status()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    @DisplayName("Solo se puede cancelar un pedido PENDING o PAID, nunca SHIPPED o posterior")
    void cancellationRules() {
        UUID customerId = UUID.randomUUID();
        OrderItem item = new OrderItem(UUID.randomUUID(), 2, new Money(new BigDecimal("10.00"), "USD"));

        Order orderPending = Order.createNew(customerId, List.of(item));
        orderPending.cancel();
        assertThat(orderPending.status()).isEqualTo(OrderStatus.CANCELLED);

        Order orderPaid = Order.createNew(customerId, List.of(item));
        orderPaid.markAsPaid();
        orderPaid.cancel();
        assertThat(orderPaid.status()).isEqualTo(OrderStatus.CANCELLED);

        Order orderShipped = Order.createNew(customerId, List.of(item));
        orderShipped.markAsPaid();
        orderShipped.markAsShipped();

        assertThatThrownBy(orderShipped::cancel)
                .isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    @DisplayName("Solo se puede enviar un pedido si está en estado PAID")
    void shippingRules() {
        UUID customerId = UUID.randomUUID();
        OrderItem item = new OrderItem(UUID.randomUUID(), 2, new Money(new BigDecimal("10.00"), "USD"));

        Order pendingOrder = Order.createNew(customerId, List.of(item));
        assertThatThrownBy(pendingOrder::markAsShipped)
                .isInstanceOf(InvalidOrderStateException.class);
    }
}

