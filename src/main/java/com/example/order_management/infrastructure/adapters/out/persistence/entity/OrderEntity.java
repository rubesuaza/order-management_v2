package com.example.order_management.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * JPA Entity for Order persistence.
 * This is separate from the domain model to maintain decoupling.
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@ToString(exclude = "items")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity {
    
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "customer_id", nullable = false, columnDefinition = "UUID")
    private UUID customerId;
    
    @Column(name = "status", nullable = false, length = 20)
    private String status;
    
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItemEntity> items = new ArrayList<>();
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderEntity that = (OrderEntity) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
