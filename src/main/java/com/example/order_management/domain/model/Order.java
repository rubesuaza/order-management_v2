package com.example.order_management.domain.model;

import com.example.order_management.domain.exception.InvalidOrderStateException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Order {
    private final OrderId id;
    private final List<OrderItem> items;
    private OrderStatus status;
    private Address shippingAddress;

    private Order(OrderId id, List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }
        this.id = id;
        this.items = new ArrayList<>(items);
        this.status = OrderStatus.PENDING;
    }

    // Factory method
    public static Order create(List<OrderItem> items) {
        return new Order(OrderId.generate(), items);
    }

    public static Order of(OrderId id, List<OrderItem> items, OrderStatus status) {
        Order order = new Order(id, items);
        order.status = status;
        return order;
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

    public Address getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(Address shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public Money getTotalAmount() {
        return items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(Money::add)
                .orElseThrow(() -> new IllegalStateException("Order must have at least one item"));
    }

    public void markAsPaid() {
        if (status != OrderStatus.PENDING) {
            throw new InvalidOrderStateException(
                String.format("Order can only be marked as PAID from PENDING status. Current status: %s", status)
            );
        }
        
        Money total = getTotalAmount();
        BigDecimal minimumAmount = new BigDecimal("10.00");
        
        if (total.getAmount().compareTo(minimumAmount) < 0) {
            throw new IllegalStateException(
                String.format("Order total must be at least 10.00 %s before being marked as PAID. Current total: %s", 
                    total.getCurrency().getCurrencyCode(), total.getAmount())
            );
        }
        
        this.status = OrderStatus.PAID;
    }

    public void markAsShipped() {
        if (status != OrderStatus.PAID) {
            throw new InvalidOrderStateException(
                String.format("Order must be PAID before shipping. Current status: %s", status)
            );
        }
        this.status = OrderStatus.SHIPPED;
    }

    public void cancel() {
        if (status == OrderStatus.SHIPPED) {
            throw new InvalidOrderStateException(
                String.format("Cannot cancel order in status SHIPPED")
            );
        }
        if (status == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException(
                String.format("Order is already CANCELLED")
            );
        }
        this.status = OrderStatus.CANCELLED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
