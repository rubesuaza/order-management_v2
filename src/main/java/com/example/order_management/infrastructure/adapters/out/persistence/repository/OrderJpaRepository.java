package com.example.order_management.infrastructure.adapters.out.persistence.repository;

import com.example.order_management.infrastructure.adapters.out.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository for OrderEntity persistence operations.
 */
@Repository
public interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID> {
    
    /**
     * Finds an OrderEntity by ID with items eagerly fetched to avoid N+1 query problem.
     */
    @Query("SELECT o FROM OrderEntity o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<OrderEntity> findByIdWithItems(@Param("id") UUID id);
}
