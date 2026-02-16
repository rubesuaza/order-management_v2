package com.example.order_management.application.ports.in;

import com.example.order_management.domain.model.Order;

import java.util.UUID;

/**
 * Input port for retrieving an order by ID.
 */
public interface GetOrderUseCase {
    
    /**
     * Retrieves an order by its ID.
     * 
     * @param orderId The order identifier
     * @return The order if found
     * @throws com.example.order_management.domain.exception.DomainException if order not found
     */
    Order getOrderById(UUID orderId);
}
