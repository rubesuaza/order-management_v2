package com.example.order_management.infrastructure.adapters.out.persistence.repository;

import com.example.order_management.infrastructure.adapters.out.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID> {
    
    @EntityGraph(attributePaths = "items")
    Optional<OrderEntity> findWithItemsById(UUID id);
}
