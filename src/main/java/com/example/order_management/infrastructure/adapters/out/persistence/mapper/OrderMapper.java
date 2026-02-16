package com.example.order_management.infrastructure.adapters.out.persistence.mapper;

import com.example.order_management.domain.model.Money;
import com.example.order_management.domain.model.Order;
import com.example.order_management.domain.model.OrderItem;
import com.example.order_management.domain.model.OrderStatus;
import com.example.order_management.infrastructure.adapters.out.persistence.entity.OrderEntity;
import com.example.order_management.infrastructure.adapters.out.persistence.entity.OrderItemEntity;
import org.springframework.stereotype.Component;

import java.util.List;
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
        
        // Use package-private reconstruction method if available, otherwise use reflection
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
     * Reconstructs an Order from persistence.
     * Uses reflection to set private fields since domain model doesn't expose a reconstruction constructor.
     */
    private Order reconstructOrder(UUID orderId, UUID customerId, List<OrderItem> items,
                                   OrderStatus status, Money totalAmount, java.time.LocalDateTime createdAt) {
        try {
            // Create order with constructor (sets status to PENDING)
            Order order = new Order(orderId, customerId, items);
            
            // Use reflection to set status, totalAmount, and createdAt
            java.lang.reflect.Field statusField = Order.class.getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(order, status);
            
            java.lang.reflect.Field totalAmountField = Order.class.getDeclaredField("totalAmount");
            totalAmountField.setAccessible(true);
            totalAmountField.set(order, totalAmount);
            
            java.lang.reflect.Field createdAtField = Order.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(order, createdAt);
            
            return order;
        } catch (Exception e) {
            throw new RuntimeException("Failed to reconstruct Order from persistence", e);
        }
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
    
    private void setOrderStatus(Order order, OrderStatus status) {
        // Use reflection or package-private method to set status
        // Since Order.status is private, we'll need to handle this differently
        // For now, we'll use a workaround by calling the appropriate state transition methods
        switch (status) {
            case PENDING:
                // Already set by constructor
                break;
            case PAID:
                try {
                    order.markAsPaid();
                } catch (Exception e) {
                    // If order cannot be marked as paid, we'll need to handle it
                    // This is a limitation of the current domain model design
                }
                break;
            case SHIPPED:
                try {
                    order.markAsPaid();
                    order.markAsShipped();
                } catch (Exception e) {
                    // Handle exception
                }
                break;
            case DELIVERED:
                try {
                    order.markAsPaid();
                    order.markAsShipped();
                    order.markAsDelivered();
                } catch (Exception e) {
                    // Handle exception
                }
                break;
            case CANCELLED:
                try {
                    order.cancel();
                } catch (Exception e) {
                    // Handle exception
                }
                break;
        }
    }
}
