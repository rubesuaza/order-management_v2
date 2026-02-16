package com.example.order_management.domain.model;

import com.example.order_management.domain.exception.DomainException;
import com.example.order_management.domain.exception.InvalidOrderStateException;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate Root representing a customer order.
 * Manages the lifecycle and business rules of an order.
 */
@Getter
public class Order {
    
    private final UUID orderId;
    private final UUID customerId;
    private OrderStatus status;
    private final LocalDateTime createdAt;
    private final List<OrderItem> items;
    private Money totalAmount;
    
    private static final BigDecimal MINIMUM_ORDER_AMOUNT = new BigDecimal("10.00");
    
    public Order(UUID orderId, UUID customerId, List<OrderItem> items) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID cannot be null");
        }
        if (items == null || items.isEmpty()) {
            throw new DomainException("An Order must have at least one OrderItem");
        }
        
        this.orderId = orderId;
        this.customerId = customerId;
        this.items = new ArrayList<>(items);
        this.status = OrderStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.totalAmount = calculateTotalAmount();
    }
    
    /**
     * Calculates the total amount by summing all items' totals.
     */
    private Money calculateTotalAmount() {
        Money total = new Money(BigDecimal.ZERO, "USD");
        for (OrderItem item : items) {
            total = total.add(item.calculateTotal());
        }
        return total;
    }
    
    /**
     * Marks the order as PAID.
     * Validates that the order total meets the minimum requirement.
     */
    public void markAsPaid() {
        if (this.status != OrderStatus.PENDING) {
            throw new InvalidOrderStateException(
                String.format("Order can only be marked as PAID when status is PENDING. Current status: %s", this.status)
            );
        }
        
        if (this.totalAmount.getAmount().compareTo(MINIMUM_ORDER_AMOUNT) < 0) {
            throw new InvalidOrderStateException(
                String.format("Order cannot be placed if total amount is less than %.2f USD. Current total: %.2f USD",
                    MINIMUM_ORDER_AMOUNT, this.totalAmount.getAmount())
            );
        }
        
        this.status = OrderStatus.PAID;
    }
    
    /**
     * Marks the order as SHIPPED.
     * Only allowed when order is in PAID status.
     */
    public void markAsShipped() {
        if (this.status != OrderStatus.PAID) {
            throw new InvalidOrderStateException(
                String.format("Order can only be SHIPPED if it is in PAID status. Current status: %s", this.status)
            );
        }
        this.status = OrderStatus.SHIPPED;
    }
    
    /**
     * Marks the order as DELIVERED.
     * Only allowed when order is in SHIPPED status.
     */
    public void markAsDelivered() {
        if (this.status != OrderStatus.SHIPPED) {
            throw new InvalidOrderStateException(
                String.format("Order can only be DELIVERED if it is in SHIPPED status. Current status: %s", this.status)
            );
        }
        this.status = OrderStatus.DELIVERED;
    }
    
    /**
     * Cancels the order.
     * Only allowed when order is in PENDING or PAID status.
     */
    public void cancel() {
        if (this.status != OrderStatus.PENDING && this.status != OrderStatus.PAID) {
            throw new InvalidOrderStateException(
                String.format("Order can only be CANCELLED if it is currently PENDING or PAID. Current status: %s", this.status)
            );
        }
        this.status = OrderStatus.CANCELLED;
    }
}
