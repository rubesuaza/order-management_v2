package com.example.management.order.domain.model.aggregate;

import com.example.management.order.domain.exception.CurrencyMismatchException;
import com.example.management.order.domain.exception.InvalidOrderStateException;
import com.example.management.order.domain.model.entity.OrderItem;
import com.example.management.order.domain.model.valueobject.Money;
import com.example.management.order.domain.model.valueobject.OrderId;
import com.example.management.order.domain.model.valueobject.OrderStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Order {

    private final OrderId id;
    private final List<OrderItem> items;
    private Money totalAmount;
    private OrderStatus status;

    private Order(OrderId id, List<OrderItem> items) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.items = new ArrayList<>(Objects.requireNonNull(items, "items must not be null"));
        this.status = OrderStatus.NEW;
        recalculateTotal();
    }

    public static Order create(OrderId id, List<OrderItem> items) {
        return new Order(id, items);
    }

    public static Order restore(OrderId id, List<OrderItem> items, OrderStatus status) {
        Order order = new Order(id, items);
        order.status = Objects.requireNonNull(status, "status must not be null");
        return order;
    }

    public void addItem(OrderItem item) {
        Objects.requireNonNull(item, "item must not be null");
        if (!items.isEmpty() && !item.getUnitPrice().currency().equals(totalAmount.currency())) {
            throw new CurrencyMismatchException("Order item currency must match order currency");
        }
        items.add(item);
        recalculateTotal();
    }

    public void removeItem(UUID itemId) {
        Objects.requireNonNull(itemId, "itemId must not be null");
        items.removeIf(item -> item.getId().equals(itemId));
        recalculateTotal();
    }

    public void place() {
        enforceMinimumTotalForPlacement();
        if (status != OrderStatus.NEW) {
            throw new InvalidOrderStateException("Order can only be placed from NEW state");
        }
        this.status = OrderStatus.PENDING;
    }

    public void pay() {
        if (status != OrderStatus.PENDING) {
            throw new InvalidOrderStateException("Order can only be paid from PENDING state");
        }
        this.status = OrderStatus.PAID;
    }

    public void cancel() {
        if (status != OrderStatus.PENDING && status != OrderStatus.PAID) {
            throw new InvalidOrderStateException("Cancel is only allowed for PENDING or PAID orders");
        }
        this.status = OrderStatus.CANCELLED;
    }

    public void ship() {
        if (status != OrderStatus.PAID) {
            throw new InvalidOrderStateException("Ship is only allowed for PAID orders");
        }
        this.status = OrderStatus.SHIPPED;
    }

    private void enforceMinimumTotalForPlacement() {
        if (!"USD".equals(totalAmount.currency())) {
            throw new InvalidOrderStateException("Order placement only supported for USD currency");
        }
        if (totalAmount.amount().compareTo(new BigDecimal("10.00")) < 0) {
            throw new InvalidOrderStateException("Order total must be at least 10.00 USD to place");
        }
    }

    private void recalculateTotal() {
        if (items.isEmpty()) {
            this.totalAmount = Money.of(BigDecimal.ZERO, "USD");
            return;
        }

        String currency = items.getFirst().getUnitPrice().currency();
        Money sum = Money.of(BigDecimal.ZERO, currency);

        for (OrderItem item : items) {
            if (!item.getUnitPrice().currency().equals(currency)) {
                throw new CurrencyMismatchException("All order items must have the same currency");
            }
            sum = sum.add(item.getSubtotal());
        }

        this.totalAmount = sum;
    }

    public OrderId getId() {
        return id;
    }

    public List<OrderItem> getItems() {
        return List.copyOf(items);
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }
}

