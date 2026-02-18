package com.example.order_management.application.services;

import com.example.order_management.application.ports.in.OrderUseCase;
import com.example.order_management.domain.Order;
import com.example.order_management.domain.OrderFactory;
import com.example.order_management.domain.OrderItem;
import com.example.order_management.domain.port.OrderRepository;
import com.example.order_management.domain.port.PaymentGatewayPort;
import com.example.order_management.domain.valueobject.Address;
import com.example.order_management.domain.valueobject.OrderId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Application service implementing order use cases. Depends only on domain and output ports.
 */
@Service
public class OrderApplicationService implements OrderUseCase {

    private final OrderRepository orderRepository;
    private final PaymentGatewayPort paymentGatewayPort;

    public OrderApplicationService(OrderRepository orderRepository, PaymentGatewayPort paymentGatewayPort) {
        this.orderRepository = orderRepository;
        this.paymentGatewayPort = paymentGatewayPort;
    }

    @Override
    @Transactional
    public Order create(OrderId orderId, List<OrderItem> items) {
        Order order = OrderFactory.create(orderId, items);
        return orderRepository.save(order);
    }

    @Override
    public Optional<Order> getById(OrderId orderId) {
        return orderRepository.findById(orderId);
    }

    @Override
    @Transactional
    public void markAsPaid(OrderId orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        paymentGatewayPort.charge(order.getTotalAmount());
        order.markAsPaid();
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void cancel(OrderId orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        order.cancel();
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void ship(OrderId orderId, Address address) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        order.ship(address);
        orderRepository.save(order);
    }
}
