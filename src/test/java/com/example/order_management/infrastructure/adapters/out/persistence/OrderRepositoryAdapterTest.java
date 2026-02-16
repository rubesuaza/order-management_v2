package com.example.order_management.infrastructure.adapters.out.persistence;

import com.example.order_management.application.ports.out.OrderRepository;
import com.example.order_management.domain.model.Money;
import com.example.order_management.domain.model.Order;
import com.example.order_management.domain.model.OrderItem;
import com.example.order_management.domain.model.OrderStatus;
import com.example.order_management.infrastructure.adapters.out.persistence.entity.OrderEntity;
import com.example.order_management.infrastructure.adapters.out.persistence.entity.OrderItemEntity;
import com.example.order_management.infrastructure.adapters.out.persistence.mapper.OrderMapper;
import com.example.order_management.infrastructure.adapters.out.persistence.repository.OrderJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Contract test for OrderRepository adapter.
 * Tests the persistence contract using an in-memory H2 database.
 */
@DataJpaTest
@Import({OrderRepositoryAdapter.class, OrderMapper.class})
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testdb"
})
class OrderRepositoryAdapterTest {
    
    @Autowired
    private OrderJpaRepository jpaRepository;
    
    @Autowired
    private OrderMapper mapper;
    
    private OrderRepository orderRepository;
    
    @BeforeEach
    void setUp() {
        orderRepository = new OrderRepositoryAdapter(jpaRepository, mapper);
    }
    
    @Test
    void shouldSaveAndRetrieveOrder() {
        // Given
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        OrderItem item = new OrderItem(productId, 2, new Money(new BigDecimal("15.50")));
        Order order = new Order(UUID.randomUUID(), customerId, List.of(item));
        
        // When
        Order saved = orderRepository.save(order);
        Optional<Order> found = orderRepository.findById(saved.getOrderId());
        
        // Then
        assertThat(found).isPresent();
        Order retrieved = found.get();
        assertThat(retrieved.getOrderId()).isEqualTo(saved.getOrderId());
        assertThat(retrieved.getCustomerId()).isEqualTo(customerId);
        assertThat(retrieved.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(retrieved.getItems()).hasSize(1);
        assertThat(retrieved.getItems().get(0).getProductId()).isEqualTo(productId);
        assertThat(retrieved.getItems().get(0).getQuantity()).isEqualTo(2);
    }
    
    @Test
    void shouldUpdateOrderStatus() {
        // Given
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        OrderItem item = new OrderItem(productId, 2, new Money(new BigDecimal("15.50")));
        Order order = new Order(UUID.randomUUID(), customerId, List.of(item));
        Order saved = orderRepository.save(order);
        
        // When - mark as paid
        saved.markAsPaid();
        Order updated = orderRepository.save(saved);
        Optional<Order> found = orderRepository.findById(updated.getOrderId());
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(OrderStatus.PAID);
    }
    
    @Test
    void shouldReturnEmptyWhenOrderNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        
        // When
        Optional<Order> found = orderRepository.findById(nonExistentId);
        
        // Then
        assertThat(found).isEmpty();
    }
    
    @Test
    void shouldPersistOrderWithMultipleItems() {
        // Given
        UUID customerId = UUID.randomUUID();
        UUID productId1 = UUID.randomUUID();
        UUID productId2 = UUID.randomUUID();
        
        OrderItem item1 = new OrderItem(productId1, 1, new Money(new BigDecimal("10.00")));
        OrderItem item2 = new OrderItem(productId2, 3, new Money(new BigDecimal("5.00")));
        Order order = new Order(UUID.randomUUID(), customerId, List.of(item1, item2));
        
        // When
        Order saved = orderRepository.save(order);
        Optional<Order> found = orderRepository.findById(saved.getOrderId());
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getItems()).hasSize(2);
        assertThat(found.get().getTotalAmount().getAmount())
            .isEqualByComparingTo(new BigDecimal("25.00"));
    }
}
