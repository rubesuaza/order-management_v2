package com.example.order_management.infrastructure.adapters.out.persistence.mapper;

import com.example.order_management.domain.model.*;
import com.example.order_management.infrastructure.adapters.out.persistence.entity.*;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {
    
    public static OrderEntity toEntity(Order order) {
        OrderEntity entity = new OrderEntity(
            order.getId().getValue(),
            order.getStatus().name(),
            order.getShippingAddress() != null ? toAddressEmbeddable(order.getShippingAddress()) : null
        );
        
        List<OrderItemEntity> itemEntities = order.getItems().stream()
            .map(item -> toOrderItemEntity(entity, item))
            .collect(Collectors.toList());
        
        entity.setItems(itemEntities);
        return entity;
    }
    
    public static Order toDomain(OrderEntity entity) {
        List<OrderItem> items = entity.getItems().stream()
            .map(OrderMapper::toOrderItem)
            .collect(Collectors.toList());
        
        Order order = Order.of(
            OrderId.of(entity.getId()),
            items,
            OrderStatus.valueOf(entity.getStatus())
        );
        
        if (entity.getShippingAddress() != null) {
            order.setShippingAddress(toAddress(entity.getShippingAddress()));
        }
        
        return order;
    }
    
    private static OrderItemEntity toOrderItemEntity(OrderEntity orderEntity, OrderItem item) {
        return new OrderItemEntity(
            orderEntity,
            item.getProductId(),
            item.getQuantity(),
            toMoneyEmbeddable(item.getUnitPrice())
        );
    }
    
    private static OrderItem toOrderItem(OrderItemEntity entity) {
        return new OrderItem(
            entity.getProductId(),
            entity.getQuantity(),
            toMoney(entity.getUnitPrice())
        );
    }
    
    private static AddressEmbeddable toAddressEmbeddable(Address address) {
        return new AddressEmbeddable(
            address.getStreet(),
            address.getCity(),
            address.getState(),
            address.getZipCode(),
            address.getCountry()
        );
    }
    
    private static Address toAddress(AddressEmbeddable embeddable) {
        return new Address(
            embeddable.getStreet(),
            embeddable.getCity(),
            embeddable.getState(),
            embeddable.getZipCode(),
            embeddable.getCountry()
        );
    }
    
    private static MoneyEmbeddable toMoneyEmbeddable(Money money) {
        return new MoneyEmbeddable(
            money.getAmount(),
            money.getCurrency().getCurrencyCode()
        );
    }
    
    private static Money toMoney(MoneyEmbeddable embeddable) {
        return new Money(
            embeddable.getAmount(),
            Currency.getInstance(embeddable.getCurrency())
        );
    }
}
