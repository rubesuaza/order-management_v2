package com.example.order_management.domain;

import com.example.order_management.domain.valueobject.Address;
import com.example.order_management.domain.valueobject.OrderId;

import java.util.List;
import java.util.Objects;

/**
 * Factory for creating Order aggregates. Ensures at least one item and valid PENDING status.
 * Supports reconstitution from persistence for infrastructure adapters.
 */
public final class OrderFactory {

    public static Order create(OrderId id, List<OrderItem> items) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(items, "items");
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }
        return new Order(id, items, new DefaultAmountCalculationStrategy());
    }

    /**
     * Reconstitutes an Order from persistence (status and shipping address may be set).
     */
    public static Order fromPersistence(OrderId id, List<OrderItem> items, OrderStatus status, Address shippingAddress) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(status, "status");
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }
        return new Order(id, items, new DefaultAmountCalculationStrategy(), status, shippingAddress);
    }
}
