package com.example.order_management.domain.ports;

import com.example.order_management.domain.model.Order;
import com.example.order_management.domain.model.OrderId;

import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(OrderId orderId);
    void delete(OrderId orderId);
}
