package com.example.management.order.domain.model.aggregate;

import com.example.management.order.domain.exception.InvalidOrderStateException;
import com.example.management.order.domain.model.entity.OrderItem;
import com.example.management.order.domain.model.valueobject.Money;
import com.example.management.order.domain.model.valueobject.OrderId;
import com.example.management.order.domain.model.valueobject.OrderStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderTest {

    @Test
    void recalculatesTotalWhenItemsAreAddedOrRemoved() {
        OrderId orderId = OrderId.of(UUID.randomUUID());
        OrderItem item1 = new OrderItem("product-1", 1, Money.of(new BigDecimal("5.00"), "USD"));
        Order order = Order.create(orderId, List.of(item1));

        assertEquals(new BigDecimal("5.00"), order.getTotalAmount().amount());

        OrderItem item2 = new OrderItem("product-2", 2, Money.of(new BigDecimal("3.00"), "USD"));
        order.addItem(item2);

        assertEquals(new BigDecimal("11.00"), order.getTotalAmount().amount());

        order.removeItem(item1.getId());

        assertEquals(new BigDecimal("6.00"), order.getTotalAmount().amount());
    }

    @Test
    void placeOrderRequiresMinimumTotalOfTenUsd() {
        OrderId orderId = OrderId.of(UUID.randomUUID());
        OrderItem item = new OrderItem("product-1", 1, Money.of(new BigDecimal("9.99"), "USD"));
        Order order = Order.create(orderId, List.of(item));

        assertThrows(InvalidOrderStateException.class, order::place);

        OrderItem additionalItem = new OrderItem("product-2", 1, Money.of(new BigDecimal("0.01"), "USD"));
        order.addItem(additionalItem);

        order.place(); // should not throw
        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    void cancelOrderOnlyAllowedForPendingOrPaidAndForbiddenForShipped() {
        Order order = createOrderWithTotal("10.00");
        order.place();

        order.cancel(); // from PENDING
        assertEquals(OrderStatus.CANCELLED, order.getStatus());

        Order paidOrder = createOrderWithTotal("20.00");
        paidOrder.place();
        paidOrder.pay();

        paidOrder.cancel(); // from PAID
        assertEquals(OrderStatus.CANCELLED, paidOrder.getStatus());

        Order shippedOrder = createOrderWithTotal("20.00");
        shippedOrder.place();
        shippedOrder.pay();
        shippedOrder.ship();

        assertThrows(InvalidOrderStateException.class, shippedOrder::cancel);
    }

    @Test
    void shipOrderRequiresPaidStatus() {
        Order order = createOrderWithTotal("20.00");

        assertThrows(InvalidOrderStateException.class, order::ship);

        order.place();
        assertThrows(InvalidOrderStateException.class, order::ship);

        order.pay();
        order.ship(); // should not throw
        assertEquals(OrderStatus.SHIPPED, order.getStatus());
    }

    private Order createOrderWithTotal(String total) {
        OrderId orderId = OrderId.of(UUID.randomUUID());
        BigDecimal amount = new BigDecimal(total);
        Money unitPrice = Money.of(amount, "USD");
        OrderItem item = new OrderItem("product-1", 1, unitPrice);
        return Order.create(orderId, List.of(item));
    }
}

