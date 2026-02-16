package com.example.order_management.infrastructure.adapters.out.persistence.repository;

import com.example.order_management.infrastructure.adapters.out.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * JPA Repository for OrderEntity persistence operations.
 */
@Repository
public interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID> {
}
