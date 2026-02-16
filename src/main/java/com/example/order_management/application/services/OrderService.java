package com.example.order_management.application.services;

import com.example.order_management.application.ports.in.CreateOrderUseCase;
import com.example.order_management.application.ports.in.GetOrderUseCase;
import com.example.order_management.application.ports.in.PayOrderUseCase;
import com.example.order_management.application.ports.out.OrderRepository;
import com.example.order_management.domain.exception.DomainException;
import com.example.order_management.domain.model.Money;
import com.example.order_management.domain.model.Order;
import com.example.order_management.domain.model.OrderItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application service implementing order use cases.
 */
@Service
public class OrderService implements CreateOrderUseCase, GetOrderUseCase, PayOrderUseCase {
    
    private final OrderRepository orderRepository;
    
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    
    @Override
    @Transactional
    public Order createOrder(UUID customerId, List<OrderItemRequest> items) {
        List<OrderItem> domainItems = items.stream()
            .map(item -> new OrderItem(
                item.productId(),
                item.quantity(),
                new Money(item.unitPrice())
            ))
            .collect(Collectors.toList());
        
        Order order = new Order(UUID.randomUUID(), customerId, domainItems);
        return orderRepository.save(order);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Order getOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new DomainException("Order not found with ID: " + orderId));
    }
    
    @Override
    @Transactional
    public Order payOrder(UUID orderId) {
        Order order = getOrderById(orderId);
        order.markAsPaid();
        return orderRepository.save(order);
    }
}
