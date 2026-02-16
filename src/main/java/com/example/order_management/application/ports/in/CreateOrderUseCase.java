package com.example.order_management.application.ports.in;

import com.example.order_management.domain.model.Order;

import java.util.List;
import java.util.UUID;

/**
 * Input port for creating a new order.
 */
public interface CreateOrderUseCase {
    
    /**
     * Creates a new order with the given customer ID and items.
     * 
     * @param customerId The customer identifier
     * @param items The list of order items (productId, quantity, unitPrice)
     * @return The created order
     */
    Order createOrder(UUID customerId, List<OrderItemRequest> items);
    
    /**
     * Request DTO for order item creation.
     */
    record OrderItemRequest(
        UUID productId,
        int quantity,
        java.math.BigDecimal unitPrice
    ) {}
}
