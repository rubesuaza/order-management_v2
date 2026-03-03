package com.example.management.domain.model.aggregate;

import com.example.management.domain.exception.InvalidItemException;
import com.example.management.domain.exception.InvalidOrderStateException;
import com.example.management.domain.model.valueobject.Address;
import com.example.management.domain.model.valueobject.Money;
import com.example.management.domain.model.valueobject.OrderId;
import com.example.management.domain.model.valueobject.OrderStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderTest {

    private Order newOrderWithUsdCurrency() {
        return Order.createNew(OrderId.newId(), new Address("Main St 1", "NYC", "NY", "10001", "USA"), "USD");
    }

    @Test
    void shouldRecalculateTotalWhenItemAdded() {
        Order order = newOrderWithUsdCurrency();

        order.addItem("PRODUCT-1", 2, Money.of(new BigDecimal("5.00"), "USD"));

        assertEquals(new BigDecimal("10.00"), order.getTotalAmount().getAmount());
        assertEquals("USD", order.getTotalAmount().getCurrency());
    }

    @Test
    void shouldRecalculateTotalWhenItemRemoved() {
        Order order = newOrderWithUsdCurrency();
        order.addItem("PRODUCT-1", 1, Money.of(new BigDecimal("5.00"), "USD"));
        order.addItem("PRODUCT-2", 1, Money.of(new BigDecimal("7.50"), "USD"));

        long itemIdToRemove = order.getItems().stream()
                .filter(item -> item.getProductId().equals("PRODUCT-2"))
                .findFirst()
                .orElseThrow()
                .getId();

        order.removeItem(itemIdToRemove);

        assertEquals(new BigDecimal("5.00"), order.getTotalAmount().getAmount());
    }

    @Test
    void shouldNotAllowNonPositiveQuantity() {
        Order order = newOrderWithUsdCurrency();

        assertThrows(InvalidItemException.class,
                () -> order.addItem("PRODUCT-1", 0, Money.of(new BigDecimal("5.00"), "USD")));
        assertThrows(InvalidItemException.class,
                () -> order.addItem("PRODUCT-1", -1, Money.of(new BigDecimal("5.00"), "USD")));
    }

    @Test
    void shouldNotAllowNegativeUnitPrice() {
        Order order = newOrderWithUsdCurrency();

        assertThrows(InvalidItemException.class,
                () -> order.addItem("PRODUCT-1", 1, Money.of(new BigDecimal("-1.00"), "USD")));
    }

    @Test
    void shouldNotAllowItemsWithDifferentCurrencyThanOrder() {
        Order order = newOrderWithUsdCurrency();

        assertThrows(InvalidItemException.class,
                () -> order.addItem("PRODUCT-1", 1, Money.of(new BigDecimal("5.00"), "EUR")));
    }

    @Test
    void shouldPlaceOrderOnlyWhenTotalAtLeastTenUsd() {
        Order order = newOrderWithUsdCurrency();
        order.addItem("PRODUCT-1", 2, Money.of(new BigDecimal("5.00"), "USD"));

        order.place();

        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    void shouldRejectPlacingOrderWhenTotalLessThanTenUsd() {
        Order order = newOrderWithUsdCurrency();
        order.addItem("PRODUCT-1", 1, Money.of(new BigDecimal("5.00"), "USD"));

        assertThrows(InvalidOrderStateException.class, order::place);
    }

    @Test
    void cancelShouldBeAllowedOnlyFromPendingOrPaid() {
        Order order = newOrderWithUsdCurrency();
        order.addItem("PRODUCT-1", 2, Money.of(new BigDecimal("5.00"), "USD"));
        order.place();

        // cancel from PENDING
        order.cancel();
        assertEquals(OrderStatus.CANCELLED, order.getStatus());

        // recreate a new order and go to PAID
        Order paidOrder = newOrderWithUsdCurrency();
        paidOrder.addItem("PRODUCT-1", 2, Money.of(new BigDecimal("5.00"), "USD"));
        paidOrder.place();
        paidOrder.pay();

        paidOrder.cancel();
        assertEquals(OrderStatus.CANCELLED, paidOrder.getStatus());
    }

    @Test
    void cancelShouldFailWhenShipped() {
        Order order = newOrderWithUsdCurrency();
        order.addItem("PRODUCT-1", 2, Money.of(new BigDecimal("5.00"), "USD"));
        order.place();
        order.pay();
        order.ship();

        assertThrows(InvalidOrderStateException.class, order::cancel);
    }

    @Test
    void shipShouldRequirePaidStatus() {
        Order pendingOrder = newOrderWithUsdCurrency();
        pendingOrder.addItem("PRODUCT-1", 2, Money.of(new BigDecimal("5.00"), "USD"));
        pendingOrder.place();

        assertThrows(InvalidOrderStateException.class, pendingOrder::ship);

        Order paidOrder = newOrderWithUsdCurrency();
        paidOrder.addItem("PRODUCT-1", 2, Money.of(new BigDecimal("5.00"), "USD"));
        paidOrder.place();
        paidOrder.pay();

        paidOrder.ship();
        assertEquals(OrderStatus.SHIPPED, paidOrder.getStatus());
    }
}

