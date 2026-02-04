package com.example.management.infrastructure.config;

import com.example.management.application.ports.in.ManageOrdersPort;
import com.example.management.application.ports.out.OrderRepository;
import com.example.management.application.services.OrderManagementService;
import com.example.management.infrastructure.adapters.out.InMemoryOrderRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires application ports with adapters. Only infrastructure imports concrete adapters.
 */
@Configuration
public class OrderManagementConfig {

    @Bean
    public OrderRepository orderRepository() {
        return new InMemoryOrderRepository();
    }

    @Bean
    public ManageOrdersPort manageOrdersPort(OrderRepository orderRepository) {
        return new OrderManagementService(orderRepository);
    }
}
