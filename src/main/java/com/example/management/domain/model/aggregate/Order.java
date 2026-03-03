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

import static java.util.Objects.requireNonNull;

public class Order {

    private final OrderId orderId;
    private final UUID customerId;
    private OrderStatus status;
    private final List<OrderItem> items;
    private Money totalAmount;

    public Order(OrderId orderId, UUID customerId, List<OrderItem> items, Money totalAmount, OrderStatus status) {
        this.orderId = requireNonNull(orderId, "orderId must not be null");
        this.customerId = requireNonNull(customerId, "customerId must not be null");
        this.items = new ArrayList<>(requireNonNull(items, "items must not be null"));
        this.totalAmount = requireNonNull(totalAmount, "totalAmount must not be null");
        this.status = requireNonNull(status, "status must not be null");
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

    public static Builder builder() {
        return new Builder();
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
        String currency = items.get(0).getUnitPrice().getCurrency();
        this.totalAmount = items.stream()
                .map(OrderItem::getSubTotal)
                .reduce(Money.zero(currency), Money::add);
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

    public static final class Builder {
        private OrderId orderId;
        private UUID customerId;
        private List<OrderItem> items = new ArrayList<>();
        private Money totalAmount;
        private OrderStatus status = OrderStatus.PENDING;

        private Builder() {
        }

        public Builder orderId(OrderId orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder customerId(UUID customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder items(List<OrderItem> items) {
            this.items = new ArrayList<>(items);
            return this;
        }

        public Builder totalAmount(Money totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public Builder status(OrderStatus status) {
            this.status = status;
            return this;
        }

        public Order build() {
            OrderId effectiveOrderId = this.orderId != null ? this.orderId : OrderId.newId();
            Money effectiveTotal = this.totalAmount != null
                    ? this.totalAmount
                    : (items.isEmpty()
                    ? Money.zero("USD")
                    : Money.zero(items.get(0).getUnitPrice().getCurrency()));

            return new Order(effectiveOrderId, customerId, items, effectiveTotal, status);
        }
    }
}

