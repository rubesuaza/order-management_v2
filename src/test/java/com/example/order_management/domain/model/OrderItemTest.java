package com.example.order_management.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OrderItem Entity Tests")
class OrderItemTest {

    @Test
    @DisplayName("Should create OrderItem with valid quantity and price")
    void shouldCreateOrderItemWithValidQuantityAndPrice() {
        // Given
        String productId = "PROD-001";
        int quantity = 5;
        Money unitPrice = new Money(new BigDecimal("25.50"), Currency.getInstance("USD"));

        // When
        OrderItem item = new OrderItem(productId, quantity, unitPrice);

        // Then
        assertThat(item.getProductId()).isEqualTo(productId);
        assertThat(item.getQuantity()).isEqualTo(quantity);
        assertThat(item.getUnitPrice()).isEqualTo(unitPrice);
    }

    @Test
    @DisplayName("Should throw exception when quantity is zero or negative")
    void shouldThrowExceptionWhenQuantityIsZeroOrNegative() {
        // Given
        String productId = "PROD-001";
        Money unitPrice = new Money(new BigDecimal("25.50"), Currency.getInstance("USD"));

        // When & Then
        assertThatThrownBy(() -> new OrderItem(productId, 0, unitPrice))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity must be greater than 0");

        assertThatThrownBy(() -> new OrderItem(productId, -1, unitPrice))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity must be greater than 0");
    }

    @Test
    @DisplayName("Should throw exception when unitPrice is null")
    void shouldThrowExceptionWhenUnitPriceIsNull() {
        // Given
        String productId = "PROD-001";

        // When & Then
        assertThatThrownBy(() -> new OrderItem(productId, 5, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unit price cannot be null");
    }

    @Test
    @DisplayName("Should calculate total price correctly")
    void shouldCalculateTotalPriceCorrectly() {
        // Given
        OrderItem item = new OrderItem("PROD-001", 3, 
                new Money(new BigDecimal("10.50"), Currency.getInstance("USD")));

        // When
        Money totalPrice = item.getTotalPrice();

        // Then
        assertThat(totalPrice.getAmount()).isEqualByComparingTo(new BigDecimal("31.50"));
        assertThat(totalPrice.getCurrency()).isEqualTo(Currency.getInstance("USD"));
    }
}
