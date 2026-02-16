package com.example.order_management.infrastructure.adapters.out.persistence;

import com.example.order_management.application.ports.out.OrderRepository;
import com.example.order_management.domain.model.Order;
import com.example.order_management.infrastructure.adapters.out.persistence.entity.OrderEntity;
import com.example.order_management.infrastructure.adapters.out.persistence.mapper.OrderMapper;
import com.example.order_management.infrastructure.adapters.out.persistence.repository.OrderJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter that implements the OrderRepository port using JPA persistence.
 */
@Component
public class OrderRepositoryAdapter implements OrderRepository {
    
    private final OrderJpaRepository jpaRepository;
    private final OrderMapper mapper;
    
    public OrderRepositoryAdapter(OrderJpaRepository jpaRepository, OrderMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }
    
    @Override
    public Order save(Order order) {
        OrderEntity entity = mapper.toEntity(order);
        OrderEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
    
    @Override
    public Optional<Order> findById(UUID orderId) {
        return jpaRepository.findById(orderId)
            .map(mapper::toDomain);
    }
}
