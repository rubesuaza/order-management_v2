package com.example.management.domain.model;

import com.example.management.domain.exception.InvalidOrderStateException;
import com.example.management.domain.valueobject.Money;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate root for Order. Enforces: at least one item, total = sum(line totals),
 * minimum 10 USD to mark as PAID, and valid state transitions (cancel only PENDING/PAID, ship only from PAID).
 */
public class Order {

    private static final java.math.BigDecimal MINIMUM_ORDER_AMOUNT = new java.math.BigDecimal("10.00");

    private final UUID orderId;
    private final UUID customerId;
    private final LocalDateTime createdAt;
    private OrderStatus status;
    private final List<OrderItem> items;
    private final Money totalAmount;

    public Order(UUID customerId, List<OrderItem> items) {
        if (customerId == null) {
            throw new IllegalArgumentException("CustomerId is required");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }
        this.orderId = UUID.randomUUID();
        this.customerId = customerId;
        this.createdAt = LocalDateTime.now();
        this.status = OrderStatus.PENDING;
        this.items = new ArrayList<>(items);
        this.totalAmount = computeTotal(items);
    }

    private static Money computeTotal(List<OrderItem> items) {
        return items.stream()
                .map(OrderItem::getLineTotal)
                .reduce(Money::add)
                .orElseThrow(() -> new IllegalArgumentException("Order must have at least one item"));
    }

    public UUID getOrderId() {
        return orderId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    private boolean meetsMinimumOrderAmount() {
        return totalAmount.getAmount().compareTo(MINIMUM_ORDER_AMOUNT) >= 0;
    }

    private boolean canBeMarkedAsPaid() {
        return status == OrderStatus.PENDING && meetsMinimumOrderAmount();
    }

    private boolean canBeCancelled() {
        return status == OrderStatus.PENDING || status == OrderStatus.PAID;
    }

    private boolean canBeShipped() {
        return status == OrderStatus.PAID;
    }

    /**
     * Transition to PAID. Only allowed when PENDING and total >= 10.00 USD.
     */
    public void markAsPaid() {
        if (status != OrderStatus.PENDING) {
            throw new InvalidOrderStateException(
                    "Order can only be marked as paid when status is PENDING; current: " + status);
        }
        if (!meetsMinimumOrderAmount()) {
            throw new InvalidOrderStateException(
                    "Order total must be at least 10.00 USD to be placed; current: " + totalAmount.getAmount());
        }
        this.status = OrderStatus.PAID;
    }

    /**
     * Transition to CANCELLED. Only allowed when PENDING or PAID.
     */
    public void cancel() {
        if (status == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException("Order is already cancelled");
        }
        if (!canBeCancelled()) {
            throw new InvalidOrderStateException(
                    "Order can only be cancelled when PENDING or PAID; cannot cancel when " + status + " (e.g. SHIPPED)");
        }
        this.status = OrderStatus.CANCELLED;
    }

    /**
     * Transition to SHIPPED. Only allowed when PAID.
     */
    public void ship() {
        if (!canBeShipped()) {
            throw new InvalidOrderStateException(
                    "Order can only be shipped when status is PAID; current: " + status);
        }
        this.status = OrderStatus.SHIPPED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(orderId, order.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }
}
