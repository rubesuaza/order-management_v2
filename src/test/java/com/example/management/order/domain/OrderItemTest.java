package com.example.management.order.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderItemTest {

    @Test
    @DisplayName("La cantidad debe ser estrictamente positiva")
    void quantityMustBePositive() {
        UUID productId = UUID.randomUUID();
        Money unitPrice = new Money(new BigDecimal("5.00"), "USD");

        assertThatThrownBy(() -> new OrderItem(productId, 0, unitPrice))
                .isInstanceOf(InvalidItemException.class);

        assertThatThrownBy(() -> new OrderItem(productId, -1, unitPrice))
                .isInstanceOf(InvalidItemException.class);
    }

    @Test
    @DisplayName("El precio unitario no puede ser negativo")
    void unitPriceMustNotBeNegative() {
        UUID productId = UUID.randomUUID();

        assertThatThrownBy(() -> new OrderItem(productId, 1, new Money(new BigDecimal("-1.00"), "USD")))
                .isInstanceOf(InvalidItemException.class);
    }

    @Test
    @DisplayName("Debe calcular el subtotal como unitPrice * quantity")
    void subtotalCalculation() {
        UUID productId = UUID.randomUUID();
        Money unitPrice = new Money(new BigDecimal("2.50"), "USD");

        OrderItem item = new OrderItem(productId, 4, unitPrice);

        Money subtotal = item.subtotal();
        assertThat(subtotal.amount()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(subtotal.currency()).isEqualTo("USD");
    }
}

