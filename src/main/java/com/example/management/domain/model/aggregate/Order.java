package com.example.management.domain.model.aggregate;

import com.example.management.domain.exception.InvalidOrderStateException;
import com.example.management.domain.model.entity.OrderItem;
import com.example.management.domain.model.valueobject.Money;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Order {

    private static final BigDecimal MINIMUM_TOTAL = new BigDecimal("10.00");

    private final OrderId id;
    private final List<OrderItem> items;
    private Money totalAmount;
    private OrderStatus status;

    private Order(OrderId id, List<OrderItem> items) {
        this.id = id;
        this.items = new ArrayList<>(items);
        recalculateTotal();
        this.status = OrderStatus.PENDING;
    }

    public static Order create(OrderId id, List<OrderItem> items) {
        Objects.requireNonNull(id, "OrderId must not be null");
        Objects.requireNonNull(items, "Items must not be null");
        if (items.isEmpty()) {
            throw new InvalidOrderStateException("Order must contain at least one item");
        }

        Order order = new Order(id, items);
        if (order.totalAmount.amount().compareTo(MINIMUM_TOTAL) < 0) {
            throw new InvalidOrderStateException("Order total must be at least " + MINIMUM_TOTAL);
        }
        return order;
    }

    public OrderId id() {
        return id;
    }

    public List<OrderItem> items() {
        return Collections.unmodifiableList(items);
    }

    public Money totalAmount() {
        return totalAmount;
    }

    public OrderStatus status() {
        return status;
    }

    public void addItem(OrderItem item) {
        Objects.requireNonNull(item, "Item must not be null");
        items.add(item);
        recalculateTotal();
    }

    public void removeItemById(int itemId) {
        items.removeIf(item -> item.id() == itemId);
        recalculateTotal();
    }

    public void cancel() {
        if (status == OrderStatus.SHIPPED) {
            throw new InvalidOrderStateException("Cannot cancel an order that has been shipped");
        }
        if (status == OrderStatus.CANCELLED) {
            return;
        }
        status = OrderStatus.CANCELLED;
    }

    public void markAsPaid() {
        if (status != OrderStatus.PENDING) {
            throw new InvalidOrderStateException("Only pending orders can be marked as paid");
        }
        status = OrderStatus.PAID;
    }

    public void ship() {
        if (status != OrderStatus.PAID) {
            throw new InvalidOrderStateException("Only paid orders can be shipped");
        }
        status = OrderStatus.SHIPPED;
    }

    private void recalculateTotal() {
        this.totalAmount = items.stream()
                .map(OrderItem::subtotal)
                .reduce((left, right) -> left.add(right))
                .orElse(null);
    }
}

