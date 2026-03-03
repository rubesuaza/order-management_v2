package com.example.management.order.infrastructure.persistence;

import com.example.management.order.domain.model.aggregate.Order;
import com.example.management.order.domain.model.entity.OrderItem;
import com.example.management.order.domain.model.valueobject.Money;
import com.example.management.order.domain.model.valueobject.OrderId;
import com.example.management.order.domain.model.valueobject.OrderStatus;
import com.example.management.order.infrastructure.persistence.jpa.OrderEntity;
import com.example.management.order.infrastructure.persistence.jpa.OrderItemEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
public class OrderPersistenceMapper {

    public OrderEntity toEntity(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.setId(order.getId().value());
        entity.setStatus(order.getStatus().name());
        entity.setTotalAmount(order.getTotalAmount().amount());
        entity.setCurrency(order.getTotalAmount().currency());

        List<OrderItemEntity> itemEntities = order.getItems().stream()
                .map(item -> toItemEntity(item, entity))
                .toList();
        entity.setItems(itemEntities);

        return entity;
    }

    public Order toDomain(OrderEntity entity) {
        List<OrderItem> items = entity.getItems().stream()
                .map(this::toDomainItem)
                .toList();

        OrderId orderId = OrderId.of(entity.getId());
        OrderStatus status = OrderStatus.valueOf(entity.getStatus());

        return Order.restore(orderId, items, status);
    }

    private OrderItemEntity toItemEntity(OrderItem item, OrderEntity orderEntity) {
        OrderItemEntity entity = new OrderItemEntity();
        UUID id = item.getId();
        entity.setId(id);
        entity.setOrder(orderEntity);
        entity.setProductId(item.getProductId());
        entity.setQuantity(item.getQuantity());
        entity.setUnitPrice(item.getUnitPrice().amount());
        entity.setCurrency(item.getUnitPrice().currency());
        return entity;
    }

    private OrderItem toDomainItem(OrderItemEntity entity) {
        Money unitPrice = Money.of(
                entity.getUnitPrice() != null ? entity.getUnitPrice() : BigDecimal.ZERO,
                entity.getCurrency()
        );
        return OrderItem.of(
                entity.getId(),
                entity.getProductId(),
                entity.getQuantity(),
                unitPrice
        );
    }
}

