package com.example.management.domain.model;

import com.example.management.domain.exception.InvalidOrderStateException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate root for the order lifecycle. An order must have at least one item.
 * Total amount is the sum of (unitPrice * quantity) for all items.
 * Minimum 10.00 USD to mark as PAID. Cancellation allowed only from PENDING or PAID; SHIPPED only from PAID.
 */
public final class Order {

    private static final BigDecimal MINIMUM_PAID_AMOUNT = new BigDecimal("10.00");

    private final UUID id;
    private final UUID customerId;
    private final LocalDateTime createdAt;
    private final List<OrderItem> items;
    private OrderStatus status;

    public Order(UUID id, UUID customerId, LocalDateTime createdAt, List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new InvalidOrderStateException("Order must have at least one OrderItem to be created");
        }
        this.id = Objects.requireNonNull(id, "Order ID cannot be null");
        this.customerId = Objects.requireNonNull(customerId, "Customer ID cannot be null");
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.items = List.copyOf(items);
        this.status = OrderStatus.PENDING;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public OrderStatus getStatus() {
        return status;
    }

    /**
     * Total amount = sum of (unitPrice * quantity) for all items.
     */
    public Money getTotalAmount() {
        return items.stream()
                .map(OrderItem::getLineTotal)
                .reduce(Money::add)
                .orElseThrow(); // invariant: at least one item
    }

    /**
     * Marks the order as PAID. Fails if total is less than 10.00 USD (same currency as items).
     */
    public void markAsPaid() {
        if (status != OrderStatus.PENDING) {
            throw new InvalidOrderStateException("Order can only be marked as PAID when PENDING");
        }
        Money total = getTotalAmount();
        if (total.getAmount().compareTo(MINIMUM_PAID_AMOUNT) < 0) {
            throw new InvalidOrderStateException(
                    "Order cannot be placed (PAID): total amount must be at least 10.00 " + total.getCurrency());
        }
        this.status = OrderStatus.PAID;
    }

    /**
     * Marks the order as SHIPPED. Allowed only when status is PAID.
     */
    public void markAsShipped() {
        if (status != OrderStatus.PAID) {
            throw new InvalidOrderStateException("Order can only be SHIPPED when status is PAID");
        }
        this.status = OrderStatus.SHIPPED;
    }

    /**
     * Cancels the order. Allowed only when PENDING or PAID. SHIPPED orders cannot be cancelled.
     */
    public void cancel() {
        requireCancellable();
        this.status = OrderStatus.CANCELLED;
    }

    private void requireCancellable() {
        if (status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED) {
            throw new InvalidOrderStateException("Order cannot be cancelled when already SHIPPED or DELIVERED");
        }
        if (status == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException("Order is already CANCELLED");
        }
    }
}
