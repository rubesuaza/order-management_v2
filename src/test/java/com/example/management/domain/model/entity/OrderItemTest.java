package com.example.management.domain.model.entity;

import com.example.management.domain.exception.InvalidItemException;
import com.example.management.domain.model.valueobject.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderItemTest {

    @Test
    void shouldCalculateSubtotalAsQuantityTimesUnitPrice() {
        UUID productId = UUID.randomUUID();
        Money unitPrice = Money.of(new BigDecimal("5.00"), "USD");

        OrderItem item = new OrderItem(1, productId, 3, unitPrice);

        assertEquals(new BigDecimal("15.00"), item.subtotal().amount());
        assertEquals("USD", item.subtotal().currency());
    }

    @Test
    void shouldRejectNonPositiveQuantity() {
        UUID productId = UUID.randomUUID();
        Money unitPrice = Money.of(new BigDecimal("5.00"), "USD");

        assertThrows(InvalidItemException.class, () -> new OrderItem(1, productId, 0, unitPrice));
        assertThrows(InvalidItemException.class, () -> new OrderItem(1, productId, -1, unitPrice));
    }

    @Test
    void shouldRejectNegativeUnitPrice() {
        UUID productId = UUID.randomUUID();
        Money negativePrice = Money.of(new BigDecimal("-1.00"), "USD");

        assertThrows(InvalidItemException.class, () -> new OrderItem(1, productId, 1, negativePrice));
    }
}

