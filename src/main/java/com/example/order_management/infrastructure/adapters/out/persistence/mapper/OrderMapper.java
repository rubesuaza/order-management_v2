package com.example.order_management.infrastructure.adapters.out.persistence.mapper;

import com.example.order_management.domain.model.Money;
import com.example.order_management.domain.model.Order;
import com.example.order_management.domain.model.OrderItem;
import com.example.order_management.domain.model.OrderStatus;
import com.example.order_management.infrastructure.adapters.out.persistence.entity.OrderEntity;
import com.example.order_management.infrastructure.adapters.out.persistence.entity.OrderItemEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper between Domain entities and JPA entities.
 */
@Component
public class OrderMapper {
    
    /**
     * Converts a domain Order to a JPA OrderEntity.
     */
    public OrderEntity toEntity(Order order) {
        OrderEntity entity = OrderEntity.builder()
            .id(order.getOrderId())
            .customerId(order.getCustomerId())
            .status(order.getStatus().name())
            .totalAmount(order.getTotalAmount().getAmount())
            .currency(order.getTotalAmount().getCurrency())
            .createdAt(order.getCreatedAt())
            .build();
        
        List<OrderItemEntity> itemEntities = order.getItems().stream()
            .map(item -> toItemEntity(item, entity))
            .collect(Collectors.toList());
        
        entity.setItems(itemEntities);
        return entity;
    }
    
    /**
     * Converts a JPA OrderEntity to a domain Order.
     */
    public Order toDomain(OrderEntity entity) {
        List<OrderItem> domainItems = entity.getItems().stream()
            .map(this::toDomainItem)
            .collect(Collectors.toList());
        
        // Use package-private reconstruction constructor to maintain domain encapsulation
        Order order = reconstructOrder(
            entity.getId(),
            entity.getCustomerId(),
            domainItems,
            OrderStatus.valueOf(entity.getStatus()),
            new Money(entity.getTotalAmount(), entity.getCurrency()),
            entity.getCreatedAt()
        );
        
        return order;
    }
    
    /**
     * Reconstructs an Order from persistence using package-private constructor.
     * This maintains domain model encapsulation and avoids reflection.
     */
    private Order reconstructOrder(UUID orderId, UUID customerId, List<OrderItem> items,
                                   OrderStatus status, Money totalAmount, java.time.LocalDateTime createdAt) {
        return new Order(orderId, customerId, items, status, totalAmount, createdAt);
    }
    
    private OrderItemEntity toItemEntity(OrderItem item, OrderEntity order) {
        return OrderItemEntity.builder()
            .order(order)
            .productId(item.getProductId())
            .quantity(item.getQuantity())
            .unitPrice(item.getUnitPrice().getAmount())
            .build();
    }
    
    private OrderItem toDomainItem(OrderItemEntity entity) {
        return new OrderItem(
            entity.getProductId(),
            entity.getQuantity(),
            new Money(entity.getUnitPrice(), getOrderCurrency(entity.getOrder()))
        );
    }
    
    private String getOrderCurrency(OrderEntity order) {
        return order != null ? order.getCurrency() : "USD";
    }
}
