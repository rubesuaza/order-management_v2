package com.example.management.domain.model.entity;

import com.example.management.domain.exception.InvalidItemException;
import com.example.management.domain.model.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class OrderItemTest {

    @Test
    @DisplayName("Se crea OrderItem válido con cantidad positiva y precio no negativo")
    void validOrderItemCreation() {
        Money unitPrice = Money.of(new BigDecimal("5.00"), "USD");

        OrderItem item = new OrderItem(1L, "PRODUCT-1", 2, unitPrice);

        assertEquals(1L, item.getId());
        assertEquals("PRODUCT-1", item.getProductId());
        assertEquals(2, item.getQuantity());
        assertEquals(unitPrice, item.getUnitPrice());
    }

    @Test
    @DisplayName("Cantidad no positiva lanza InvalidItemException")
    void nonPositiveQuantityThrowsException() {
        Money unitPrice = Money.of(new BigDecimal("5.00"), "USD");

        assertThrows(InvalidItemException.class, () -> new OrderItem(1L, "PRODUCT-1", 0, unitPrice));
        assertThrows(InvalidItemException.class, () -> new OrderItem(1L, "PRODUCT-1", -1, unitPrice));
    }

    @Test
    @DisplayName("Precio negativo lanza InvalidItemException")
    void negativePriceThrowsException() {
        Money negativePrice = Money.of(new BigDecimal("-1.00"), "USD");

        assertThrows(InvalidItemException.class, () -> new OrderItem(1L, "PRODUCT-1", 1, negativePrice));
    }
}

