package com.example.order_management.domain.model;

import com.example.order_management.domain.exception.InvalidItemException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OrderItem Tests")
class OrderItemTest {

    private final UUID productId = UUID.randomUUID();
    private final Money unitPrice = new Money(new BigDecimal("10.00"));

    @Test
    @DisplayName("Should create OrderItem with valid attributes")
    void shouldCreateOrderItemWithValidAttributes() {
        OrderItem item = new OrderItem(productId, 5, unitPrice);
        
        assertThat(item.getProductId()).isEqualTo(productId);
        assertThat(item.getQuantity()).isEqualTo(5);
        assertThat(item.getUnitPrice()).isEqualTo(unitPrice);
    }

    @Test
    @DisplayName("Should throw InvalidItemException when quantity is zero")
    void shouldThrowExceptionWhenQuantityIsZero() {
        assertThatThrownBy(() -> new OrderItem(productId, 0, unitPrice))
                .isInstanceOf(InvalidItemException.class)
                .hasMessageContaining("quantity");
    }

    @Test
    @DisplayName("Should throw InvalidItemException when quantity is negative")
    void shouldThrowExceptionWhenQuantityIsNegative() {
        assertThatThrownBy(() -> new OrderItem(productId, -1, unitPrice))
                .isInstanceOf(InvalidItemException.class)
                .hasMessageContaining("quantity");
    }

    @Test
    @DisplayName("Should throw InvalidItemException when unitPrice is negative")
    void shouldThrowExceptionWhenUnitPriceIsNegative() {
        Money negativePrice = new Money(new BigDecimal("-5.00"));
        
        assertThatThrownBy(() -> new OrderItem(productId, 5, negativePrice))
                .isInstanceOf(InvalidItemException.class)
                .hasMessageContaining("price");
    }

    @Test
    @DisplayName("Should calculate total price correctly")
    void shouldCalculateTotalPriceCorrectly() {
        OrderItem item = new OrderItem(productId, 3, unitPrice);
        
        Money total = item.calculateTotal();
        
        assertThat(total.getAmount()).isEqualByComparingTo(new BigDecimal("30.00"));
        assertThat(total.getCurrency()).isEqualTo("USD");
    }

    @Test
    @DisplayName("Should allow zero price")
    void shouldAllowZeroPrice() {
        Money zeroPrice = new Money(BigDecimal.ZERO);
        OrderItem item = new OrderItem(productId, 1, zeroPrice);
        
        assertThat(item.getUnitPrice()).isEqualTo(zeroPrice);
    }
}
