package com.example.order_management.application.ports.in;

import com.example.order_management.domain.model.Order;

import java.util.UUID;

/**
 * Input port for processing payment for an order.
 */
public interface PayOrderUseCase {
    
    /**
     * Processes payment for an order, changing its status to PAID.
     * 
     * @param orderId The order identifier
     * @return The updated order
     * @throws com.example.order_management.domain.exception.InvalidOrderStateException if order cannot be paid
     */
    Order payOrder(UUID orderId);
}
