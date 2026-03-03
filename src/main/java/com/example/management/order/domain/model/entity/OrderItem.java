package com.example.management.order.domain.model.entity;

import com.example.management.order.domain.exception.InvalidItemException;
import com.example.management.order.domain.model.valueobject.Money;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public class OrderItem {

    private final UUID id;
    private final String productId;
    private final int quantity;
    private final Money unitPrice;

    public OrderItem(String productId, int quantity, Money unitPrice) {
        this(UUID.randomUUID(), productId, quantity, unitPrice);
    }

    private OrderItem(UUID id, String productId, int quantity, Money unitPrice) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.productId = Objects.requireNonNull(productId, "productId must not be null");
        this.unitPrice = Objects.requireNonNull(unitPrice, "unitPrice must not be null");

        if (quantity <= 0) {
            throw new InvalidItemException("Quantity must be greater than zero");
        }
        if (unitPrice.amount().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidItemException("Unit price must not be negative");
        }

        this.quantity = quantity;
    }

    public static OrderItem of(UUID id, String productId, int quantity, Money unitPrice) {
        return new OrderItem(id, productId, quantity, unitPrice);
    }

    public UUID getId() {
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

    public Money getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}

