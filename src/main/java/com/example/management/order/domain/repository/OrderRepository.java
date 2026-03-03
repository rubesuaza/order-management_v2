package com.example.management.order.domain.repository;

import com.example.management.order.domain.model.aggregate.Order;
import com.example.management.order.domain.model.valueobject.OrderId;
import com.example.management.order.domain.model.valueobject.OrderStatus;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(OrderId id);

    List<Order> findByStatus(OrderStatus status);
}

