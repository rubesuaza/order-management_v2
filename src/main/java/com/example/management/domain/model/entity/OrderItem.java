package com.example.management.domain.model.entity;

import com.example.management.domain.exception.InvalidItemException;
import com.example.management.domain.model.valueobject.Money;

import java.math.BigDecimal;
import java.util.UUID;

public class OrderItem {

    private final int id;
    private final UUID productId;
    private final int quantity;
    private final Money unitPrice;

    public OrderItem(int id, UUID productId, int quantity, Money unitPrice) {
        if (quantity <= 0) {
            throw new InvalidItemException("Quantity must be greater than zero");
        }
        if (unitPrice == null) {
            throw new InvalidItemException("Unit price must not be null");
        }
        if (unitPrice.amount().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidItemException("Unit price must not be negative");
        }
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public int id() {
        return id;
    }

    public UUID productId() {
        return productId;
    }

    public int quantity() {
        return quantity;
    }

    public Money unitPrice() {
        return unitPrice;
    }

    public Money subtotal() {
        return unitPrice.multiply(quantity);
    }
}

