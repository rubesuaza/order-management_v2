package com.example.management.domain.model.aggregate;

import com.example.management.domain.exception.InvalidOrderStateException;
import com.example.management.domain.model.entity.OrderItem;
import com.example.management.domain.model.valueobject.Address;
import com.example.management.domain.model.valueobject.Money;
import com.example.management.domain.model.valueobject.OrderId;
import com.example.management.domain.model.valueobject.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    private Money usd(double amount) {
        return Money.of(BigDecimal.valueOf(amount).setScale(2), "USD");
    }

    private Address anyAddress() {
        return new Address("Street 1", "City", "00000", "US");
    }

    @Test
    @DisplayName("Al agregar o eliminar items el total se recalcula automáticamente")
    void totalAmountIsRecalculatedWhenItemsChange() {
        OrderId id = new OrderId(UUID.randomUUID());
        OrderItem item1 = new OrderItem(1L, "P1", 1, usd(5.00));
        OrderItem item2 = new OrderItem(2L, "P2", 2, usd(3.00));

        Order order = Order.create(id, anyAddress(), List.of(item1));
        assertEquals(usd(5.00), order.getTotalAmount());

        order.addItem(item2);
        assertEquals(usd(11.00), order.getTotalAmount());

        order.removeItemById(1L);
        assertEquals(usd(6.00), order.getTotalAmount());
    }

    @Test
    @DisplayName("No se puede colocar una orden con total menor a 10.00 USD")
    void cannotPlaceOrderWithTotalLessThanTenUsd() {
        OrderId id = new OrderId(UUID.randomUUID());
        OrderItem item = new OrderItem(1L, "P1", 1, usd(9.99));

        Order order = Order.create(id, anyAddress(), List.of(item));

        assertThrows(InvalidOrderStateException.class, order::place);
    }

    @Test
    @DisplayName("Se puede colocar una orden con total mayor o igual a 10.00 USD")
    void canPlaceOrderWithTotalAtLeastTenUsd() {
        OrderId id = new OrderId(UUID.randomUUID());
        OrderItem item = new OrderItem(1L, "P1", 2, usd(5.00));

        Order order = Order.create(id, anyAddress(), List.of(item));

        order.place();

        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    @DisplayName("Sólo se puede pagar una orden en estado PENDING")
    void payOnlyFromPending() {
        Order order = samplePlacedOrder();

        order.pay();

        assertEquals(OrderStatus.PAID, order.getStatus());

        assertThrows(InvalidOrderStateException.class, order::pay);
    }

    @Test
    @DisplayName("Cancelar sólo está permitido desde PENDING o PAID y prohibido si SHIPPED")
    void cancelRules() {
        Order pending = samplePlacedOrder();
        pending.cancel();
        assertEquals(OrderStatus.CANCELLED, pending.getStatus());

        Order paid = samplePlacedOrder();
        paid.pay();
        paid.cancel();
        assertEquals(OrderStatus.CANCELLED, paid.getStatus());

        Order shipped = samplePlacedOrder();
        shipped.pay();
        shipped.ship();

        assertThrows(InvalidOrderStateException.class, shipped::cancel);
    }

    @Test
    @DisplayName("Para enviar, la orden debe estar en estado PAID")
    void shipRequiresPaid() {
        Order order = samplePlacedOrder();

        assertThrows(InvalidOrderStateException.class, order::ship);

        order.pay();
        order.ship();

        assertEquals(OrderStatus.SHIPPED, order.getStatus());
    }

    private Order samplePlacedOrder() {
        OrderId id = new OrderId(UUID.randomUUID());
        OrderItem item = new OrderItem(1L, "P1", 2, usd(5.00));
        Order order = Order.create(id, anyAddress(), List.of(item));
        order.place();
        return order;
    }
}

