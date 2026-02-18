package com.example.order_management.domain;

import com.example.order_management.domain.valueobject.Money;

import java.util.Objects;

/**
 * Entity within Order aggregate. Invariants: quantity > 0, unitPrice cannot be negative.
 */
public final class OrderItem {

    private final String productId;
    private final Money unitPrice;
    private final int quantity;

    private OrderItem(String productId, Money unitPrice, int quantity) {
        this.productId = Objects.requireNonNull(productId, "productId");
        this.unitPrice = Objects.requireNonNull(unitPrice, "unitPrice");
        this.quantity = quantity;
    }

    public static OrderItem create(String productId, Money unitPrice, int quantity) {
        if (unitPrice.getAmount().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        return new OrderItem(productId, unitPrice, quantity);
    }

    public String getProductId() {
        return productId;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public Money getLineTotal() {
        return unitPrice.multiply(quantity);
    }
}
