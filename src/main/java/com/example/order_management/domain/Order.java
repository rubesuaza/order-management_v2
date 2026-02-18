package com.example.order_management.domain;

import com.example.order_management.domain.exception.InvalidOrderStateException;
import com.example.order_management.domain.valueobject.Address;
import com.example.order_management.domain.valueobject.Money;
import com.example.order_management.domain.valueobject.OrderId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate root. Invariants: at least one OrderItem; minimum 10.00 USD before PAID.
 * State: CANCELLED only from PENDING or PAID; SHIPPED only from PAID.
 */
public final class Order {

    private static final BigDecimal MIN_AMOUNT_FOR_PAID = new BigDecimal("10.00");

    private final OrderId id;
    private final List<OrderItem> items;
    private final AmountCalculationStrategy amountStrategy;
    private OrderStatus status;
    private Address shippingAddress;

    Order(OrderId id, List<OrderItem> items, AmountCalculationStrategy amountStrategy) {
        this.id = Objects.requireNonNull(id, "id");
        this.items = new ArrayList<>(Objects.requireNonNull(items, "items"));
        this.amountStrategy = amountStrategy != null ? amountStrategy : new DefaultAmountCalculationStrategy();
        this.status = OrderStatus.PENDING;
    }

    /**
     * Reconstitutes an Order from persistence (used by infrastructure adapters).
     */
    Order(OrderId id, List<OrderItem> items, AmountCalculationStrategy amountStrategy,
          OrderStatus status, Address shippingAddress) {
        this.id = Objects.requireNonNull(id, "id");
        this.items = new ArrayList<>(Objects.requireNonNull(items, "items"));
        this.amountStrategy = amountStrategy != null ? amountStrategy : new DefaultAmountCalculationStrategy();
        this.status = Objects.requireNonNull(status, "status");
        this.shippingAddress = shippingAddress;
    }

    public OrderId getId() {
        return id;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Money getTotalAmount() {
        return amountStrategy.calculateTotal(items);
    }

    public Address getShippingAddress() {
        return shippingAddress;
    }

    public void markAsPaid() {
        if (status != OrderStatus.PENDING) {
            throw new InvalidOrderStateException("Can only mark as PAID from PENDING");
        }
        if (isBelowMinimumAmountForPaid()) {
            throw new InvalidOrderStateException("Order total must be at least 10.00 USD before marking as PAID");
        }
        status = OrderStatus.PAID;
    }

    private boolean isBelowMinimumAmountForPaid() {
        return getTotalAmount().getAmount().compareTo(MIN_AMOUNT_FOR_PAID) < 0;
    }

    public void cancel() {
        if (status == OrderStatus.SHIPPED) {
            throw new InvalidOrderStateException("Cannot transition to CANCELLED from SHIPPED");
        }
        if (status == OrderStatus.CANCELLED) {
            return;
        }
        status = OrderStatus.CANCELLED;
    }

    public void ship(Address address) {
        if (status != OrderStatus.PAID) {
            throw new InvalidOrderStateException("Cannot transition to SHIPPED from " + status + "; only from PAID");
        }
        this.shippingAddress = Objects.requireNonNull(address, "address");
        status = OrderStatus.SHIPPED;
    }
}
