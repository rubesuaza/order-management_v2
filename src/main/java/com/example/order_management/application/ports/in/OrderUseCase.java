package com.example.order_management.application.ports.in;

import com.example.order_management.domain.Order;
import com.example.order_management.domain.OrderItem;
import com.example.order_management.domain.valueobject.Address;
import com.example.order_management.domain.valueobject.OrderId;

import java.util.List;
import java.util.Optional;

/**
 * Input port (use case) for order operations. Implemented by application services.
 */
public interface OrderUseCase {

    Order create(OrderId orderId, List<OrderItem> items);

    Optional<Order> getById(OrderId orderId);

    void markAsPaid(OrderId orderId);

    void cancel(OrderId orderId);

    void ship(OrderId orderId, Address address);
}
