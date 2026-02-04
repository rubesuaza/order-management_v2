package com.example.management.application.services;

import com.example.management.application.ports.in.ManageOrdersPort;
import com.example.management.application.ports.out.OrderRepository;
import com.example.management.domain.model.Money;
import com.example.management.domain.model.Order;
import com.example.management.domain.model.OrderItem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service that implements order management use cases. Depends only on output ports (repository).
 */
public class OrderManagementService implements ManageOrdersPort {

    private final OrderRepository orderRepository;

    public OrderManagementService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Order createOrder(UUID customerId, List<OrderItemCommand> items) {
        List<OrderItem> domainItems = items.stream()
                .map(cmd -> new OrderItem(
                        cmd.productId(),
                        cmd.quantity(),
                        new Money(cmd.unitPriceAmount(), cmd.unitPriceCurrency() != null ? cmd.unitPriceCurrency() : "USD")))
                .toList();
        Order order = new Order(UUID.randomUUID(), customerId, LocalDateTime.now(), domainItems);
        return orderRepository.save(order);
    }

    @Override
    public Optional<Order> getOrder(UUID orderId) {
        return orderRepository.findById(orderId);
    }

    @Override
    public List<Order> listOrders() {
        return orderRepository.findAll();
    }

    @Override
    public void markOrderAsPaid(UUID orderId) {
        Order order = findOrderOrThrow(orderId);
        order.markAsPaid();
        orderRepository.save(order);
    }

    @Override
    public void markOrderAsShipped(UUID orderId) {
        Order order = findOrderOrThrow(orderId);
        order.markAsShipped();
        orderRepository.save(order);
    }

    @Override
    public void cancelOrder(UUID orderId) {
        Order order = findOrderOrThrow(orderId);
        order.cancel();
        orderRepository.save(order);
    }

    private Order findOrderOrThrow(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    }
}
