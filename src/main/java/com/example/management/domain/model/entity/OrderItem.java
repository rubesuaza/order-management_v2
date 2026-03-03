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

    public static Builder builder() {
        return new Builder();
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

    public static final class Builder {
        private UUID id;
        private UUID productId;
        private int quantity;
        private Money unitPrice;

        private Builder() {
        }

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder productId(UUID productId) {
            this.productId = productId;
            return this;
        }

        public Builder quantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder unitPrice(Money unitPrice) {
            this.unitPrice = unitPrice;
            return this;
        }

        public OrderItem build() {
            UUID effectiveId = this.id != null ? this.id : UUID.randomUUID();
            return new OrderItem(effectiveId, productId, quantity, unitPrice);
        }
    }
}

