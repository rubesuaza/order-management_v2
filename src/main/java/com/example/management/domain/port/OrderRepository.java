package com.example.management.domain.port;

import com.example.management.domain.model.aggregate.Order;
import com.example.management.domain.model.aggregate.OrderId;

import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(OrderId id);
}

