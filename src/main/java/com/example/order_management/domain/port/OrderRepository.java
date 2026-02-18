package com.example.order_management.domain.port;

import com.example.order_management.domain.Order;
import com.example.order_management.domain.valueobject.OrderId;

import java.util.Optional;

/**
 * Port for persisting and retrieving Order aggregates. Implemented by infrastructure.
 */
public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(OrderId orderId);
}
