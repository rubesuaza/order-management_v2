package com.example.order_management.infrastructure.adapters.out.persistence;

import com.example.order_management.domain.Order;
import com.example.order_management.domain.OrderFactory;
import com.example.order_management.domain.OrderItem;
import com.example.order_management.domain.OrderStatus;
import com.example.order_management.domain.port.OrderRepository;
import com.example.order_management.domain.valueobject.Address;
import com.example.order_management.domain.valueobject.Money;
import com.example.order_management.domain.valueobject.OrderId;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class OrderRepositoryAdapter implements OrderRepository {

    private final OrderJpaRepository jpaRepository;

    public OrderRepositoryAdapter(OrderJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Order save(Order order) {
        OrderEntity entity = toEntity(order);
        OrderEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Order> findById(OrderId orderId) {
        return jpaRepository.findById(orderId.getValue())
                .map(this::toDomain);
    }

    private OrderEntity toEntity(Order order) {
        OrderEntity entity = new OrderEntity(order.getId().getValue(), order.getStatus());
        if (order.getShippingAddress() != null) {
            entity.setShippingStreet(order.getShippingAddress().getStreet());
            entity.setShippingCity(order.getShippingAddress().getCity());
            entity.setShippingZipCode(order.getShippingAddress().getZipCode());
            entity.setShippingCountry(order.getShippingAddress().getCountry());
        }
        List<OrderItemEntity> itemEntities = order.getItems().stream()
                .map(item -> new OrderItemEntity(entity, item.getProductId(),
                        item.getUnitPrice().getAmount(), item.getUnitPrice().getCurrency(), item.getQuantity()))
                .collect(Collectors.toList());
        entity.setItems(itemEntities);
        return entity;
    }

    private Order toDomain(OrderEntity entity) {
        List<OrderItem> items = entity.getItems().stream()
                .map(item -> OrderItem.create(item.getProductId(),
                        Money.of(item.getUnitPriceAmount(), item.getUnitPriceCurrency()),
                        item.getQuantity()))
                .collect(Collectors.toList());
        Address shippingAddress = null;
        if (entity.getShippingStreet() != null && entity.getShippingCity() != null
                && entity.getShippingZipCode() != null && entity.getShippingCountry() != null) {
            shippingAddress = Address.of(entity.getShippingStreet(), entity.getShippingCity(),
                    entity.getShippingZipCode(), entity.getShippingCountry());
        }
        return OrderFactory.fromPersistence(
                OrderId.of(entity.getId()),
                items,
                entity.getStatus(),
                shippingAddress
        );
    }
}
