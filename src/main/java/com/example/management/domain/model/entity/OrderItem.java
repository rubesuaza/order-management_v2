package com.example.management.domain.model.entity;

import com.example.management.domain.exception.InvalidItemException;
import com.example.management.domain.model.valueobject.Money;

import java.util.Objects;

/**
 * Entidad de línea de orden.
 */
public class OrderItem {

    private final long id;
    private final String productId;
    private final int quantity;
    private final Money unitPrice;

    public OrderItem(long id, String productId, int quantity, Money unitPrice) {
        if (quantity <= 0) {
            throw new InvalidItemException("Quantity must be positive");
        }
        Objects.requireNonNull(unitPrice, "unitPrice must not be null");
        if (unitPrice.amount().signum() < 0) {
            throw new InvalidItemException("Unit price must not be negative");
        }
        this.id = id;
        this.productId = Objects.requireNonNull(productId, "productId must not be null");
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public long getId() {
        return id;
    }

    public String getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    public Money subtotal() {
        return unitPrice.multiply(quantity);
    }
}

