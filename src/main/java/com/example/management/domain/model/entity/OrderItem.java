package com.example.management.domain.model.entity;

import com.example.management.domain.exception.InvalidItemException;
import com.example.management.domain.model.valueobject.Money;

import java.util.Objects;
import java.util.UUID;

public class OrderItem {

    private final UUID id;
    private final UUID productId;
    private int quantity;
    private final Money unitPrice;

    public OrderItem(UUID id, UUID productId, int quantity, Money unitPrice) {
        if (quantity <= 0) {
            throw new InvalidItemException("Quantity must be greater than zero");
        }
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.productId = Objects.requireNonNull(productId, "productId must not be null");
        this.unitPrice = Objects.requireNonNull(unitPrice, "unitPrice must not be null");
        this.quantity = quantity;
    }

    public UUID getId() {
        return id;
    }

    public UUID getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    public Money getSubTotal() {
        return unitPrice.multiply(quantity);
    }
}

