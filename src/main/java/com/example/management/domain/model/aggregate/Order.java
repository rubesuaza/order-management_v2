package com.example.management.domain.model.aggregate;

import com.example.management.domain.exception.InvalidOrderStateException;
import com.example.management.domain.model.entity.OrderItem;
import com.example.management.domain.model.valueobject.Money;
import com.example.management.domain.model.valueobject.OrderId;
import com.example.management.domain.model.valueobject.OrderStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Order {

    private final OrderId orderId;
    private final UUID customerId;
    private OrderStatus status;
    private final List<OrderItem> items;
    private Money totalAmount;

    public Order(OrderId orderId, UUID customerId, List<OrderItem> items, Money totalAmount, OrderStatus status) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.items = new ArrayList<>(items);
        this.totalAmount = totalAmount;
        this.status = status;
        recalculateTotal();
    }

    public static Order createNew(UUID customerId, List<OrderItem> items, String currency) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        Order newOrder = new Order(
                OrderId.newId(),
                customerId,
                items,
                Money.zero(currency),
                OrderStatus.PENDING
        );
        newOrder.ensureMinimumTotal();
        return newOrder;
    }

    public void pay() {
        if (status != OrderStatus.PENDING) {
            throw new InvalidOrderStateException("Order must be PENDING to be paid");
        }
        status = OrderStatus.PAID;
    }

    public void cancel() {
        if (status == OrderStatus.SHIPPED) {
            throw new InvalidOrderStateException("Cannot cancel a shipped order");
        }
        status = OrderStatus.CANCELLED;
    }

    public void ship() {
        if (status != OrderStatus.PAID) {
            throw new InvalidOrderStateException("Order must be PAID to be shipped");
        }
        status = OrderStatus.SHIPPED;
    }

    private void recalculateTotal() {
        if (items.isEmpty()) {
            this.totalAmount = Money.zero(totalAmount != null ? totalAmount.getCurrency() : "USD");
            return;
        }
        Money runningTotal = Money.zero(items.get(0).getUnitPrice().getCurrency());
        for (OrderItem item : items) {
            runningTotal = runningTotal.add(item.getSubTotal());
        }
        this.totalAmount = runningTotal;
    }

    private void ensureMinimumTotal() {
        BigDecimal minimum = new BigDecimal("10.00");
        if (totalAmount.getAmount().compareTo(minimum) < 0) {
            throw new InvalidOrderStateException("Order total must be at least 10.00");
        }
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public UUID getCustomerId() {
        return customerId;
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
}

