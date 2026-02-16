package com.example.order_management.domain.model;

import com.example.order_management.domain.exception.InvalidItemException;
import lombok.Getter;

import java.util.UUID;

/**
 * Entity representing an item within an Order.
 */
@Getter
public class OrderItem {
    
    private final UUID productId;
    private final int quantity;
    private final Money unitPrice;
    
    public OrderItem(UUID productId, int quantity, Money unitPrice) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (quantity <= 0) {
            throw new InvalidItemException("Quantity must be strictly greater than zero");
        }
        if (unitPrice == null) {
            throw new IllegalArgumentException("Unit price cannot be null");
        }
        if (unitPrice.getAmount().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new InvalidItemException("Unit price cannot be negative");
        }
        
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }
    
    /**
     * Calculates the total price for this item (unitPrice * quantity).
     */
    public Money calculateTotal() {
        return unitPrice.multiply(quantity);
    }
}
