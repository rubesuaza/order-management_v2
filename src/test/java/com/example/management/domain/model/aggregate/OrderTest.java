package com.example.management.domain.model.aggregate;

import com.example.management.domain.exception.InvalidOrderStateException;
import com.example.management.domain.model.entity.OrderItem;
import com.example.management.domain.model.valueobject.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderTest {

    @Test
    void createOrderShouldCalculateTotalFromItems() {
        OrderItem item1 = new OrderItem(1, UUID.randomUUID(), 2, Money.of(new BigDecimal("5.00"), "USD"));
        OrderItem item2 = new OrderItem(2, UUID.randomUUID(), 1, Money.of(new BigDecimal("7.50"), "USD"));

        Order order = Order.create(OrderId.newId(), List.of(item1, item2));

        assertEquals(new BigDecimal("17.50"), order.totalAmount().amount());
        assertEquals("USD", order.totalAmount().currency());
    }

    @Test
    void orderShouldRecalculateTotalWhenAddingItem() {
        OrderItem item1 = new OrderItem(1, UUID.randomUUID(), 1, Money.of(new BigDecimal("10.00"), "USD"));
        Order order = Order.create(OrderId.newId(), List.of(item1));

        OrderItem item2 = new OrderItem(2, UUID.randomUUID(), 1, Money.of(new BigDecimal("5.00"), "USD"));
        order.addItem(item2);

        assertEquals(new BigDecimal("15.00"), order.totalAmount().amount());
    }

    @Test
    void orderShouldRecalculateTotalWhenRemovingItem() {
        OrderItem item1 = new OrderItem(1, UUID.randomUUID(), 1, Money.of(new BigDecimal("10.00"), "USD"));
        OrderItem item2 = new OrderItem(2, UUID.randomUUID(), 1, Money.of(new BigDecimal("5.00"), "USD"));
        Order order = Order.create(OrderId.newId(), List.of(item1, item2));

        order.removeItemById(2);

        assertEquals(new BigDecimal("10.00"), order.totalAmount().amount());
    }

    @Test
    void shouldRejectOrderWithTotalBelowMinimum() {
        OrderItem item = new OrderItem(1, UUID.randomUUID(), 1, Money.of(new BigDecimal("9.99"), "USD"));

        assertThrows(InvalidOrderStateException.class, () -> Order.create(OrderId.newId(), List.of(item)));
    }

    @Test
    void cancelShouldBeAllowedWhenPendingOrPaid() {
        OrderItem item = new OrderItem(1, UUID.randomUUID(), 1, Money.of(new BigDecimal("10.00"), "USD"));
        Order pendingOrder = Order.create(OrderId.newId(), List.of(item));

        pendingOrder.cancel();
        assertEquals(OrderStatus.CANCELLED, pendingOrder.status());

        Order paidOrder = Order.create(OrderId.newId(), List.of(item));
        paidOrder.markAsPaid();
        paidOrder.cancel();
        assertEquals(OrderStatus.CANCELLED, paidOrder.status());
    }

    @Test
    void cancelShouldNotBeAllowedWhenShipped() {
        OrderItem item = new OrderItem(1, UUID.randomUUID(), 1, Money.of(new BigDecimal("10.00"), "USD"));
        Order order = Order.create(OrderId.newId(), List.of(item));

        order.markAsPaid();
        order.ship();

        assertThrows(InvalidOrderStateException.class, order::cancel);
    }

    @Test
    void shipShouldRequirePaidStatus() {
        OrderItem item = new OrderItem(1, UUID.randomUUID(), 1, Money.of(new BigDecimal("10.00"), "USD"));
        Order order = Order.create(OrderId.newId(), List.of(item));

        assertThrows(InvalidOrderStateException.class, order::ship);

        order.markAsPaid();
        order.ship();

        assertEquals(OrderStatus.SHIPPED, order.status());
    }
}

