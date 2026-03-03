package com.example.management.order.domain.model.entity;

import com.example.management.order.domain.exception.InvalidItemException;
import com.example.management.order.domain.model.valueobject.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderItemTest {

    @Test
    void createsValidOrderItemWithPositiveQuantityAndPrice() {
        Money unitPrice = Money.of(new BigDecimal("5.00"), "USD");
        OrderItem item = new OrderItem("product-1", 2, unitPrice);

        assertEquals("product-1", item.getProductId());
        assertEquals(2, item.getQuantity());
        assertEquals(new BigDecimal("10.00"), item.getSubtotal().amount());
    }

    @Test
    void rejectsZeroOrNegativeQuantity() {
        Money unitPrice = Money.of(new BigDecimal("5.00"), "USD");

        assertThrows(InvalidItemException.class, () -> new OrderItem("product-1", 0, unitPrice));
        assertThrows(InvalidItemException.class, () -> new OrderItem("product-1", -1, unitPrice));
    }

    @Test
    void rejectsNegativeUnitPrice() {
        Money negativePrice = Money.of(new BigDecimal("-1.00"), "USD");

        assertThrows(InvalidItemException.class, () -> new OrderItem("product-1", 1, negativePrice));
    }
}

