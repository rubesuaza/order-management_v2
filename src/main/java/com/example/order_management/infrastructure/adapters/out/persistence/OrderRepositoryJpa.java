package com.example.order_management.infrastructure.adapters.out.persistence;

import com.example.order_management.domain.model.Order;
import com.example.order_management.domain.model.OrderId;
import com.example.order_management.domain.ports.OrderRepository;
import com.example.order_management.infrastructure.adapters.out.persistence.entity.OrderEntity;
import com.example.order_management.infrastructure.adapters.out.persistence.mapper.OrderMapper;
import com.example.order_management.infrastructure.adapters.out.persistence.repository.OrderJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrderRepositoryJpa implements OrderRepository {
    
    private final OrderJpaRepository jpaRepository;
    
    public OrderRepositoryJpa(OrderJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public Order save(Order order) {
        OrderEntity entity = OrderMapper.toEntity(order);
        OrderEntity savedEntity = jpaRepository.save(entity);
        return OrderMapper.toDomain(savedEntity);
    }
    
    @Override
    public Optional<Order> findById(OrderId orderId) {
        return jpaRepository.findById(orderId.getValue())
            .map(OrderMapper::toDomain);
    }
    
    @Override
    public void delete(OrderId orderId) {
        jpaRepository.deleteById(orderId.getValue());
    }
}
