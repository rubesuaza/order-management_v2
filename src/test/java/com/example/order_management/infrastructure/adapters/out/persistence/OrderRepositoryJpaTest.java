package com.example.order_management.infrastructure.adapters.out.persistence;

import com.example.order_management.domain.model.*;
import com.example.order_management.domain.ports.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(OrderRepositoryJpa.class)
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testdb"
})
@DisplayName("OrderRepositoryJpa Integration Tests")
class OrderRepositoryJpaTest {
    
    @Autowired
    private OrderRepository orderRepository;
    
    private Order testOrder;
    
    @BeforeEach
    void setUp() {
        OrderItem item1 = new OrderItem("PROD-001", 2, 
            new Money(new BigDecimal("10.00"), Currency.getInstance("USD")));
        OrderItem item2 = new OrderItem("PROD-002", 1, 
            new Money(new BigDecimal("15.00"), Currency.getInstance("USD")));
        
        testOrder = Order.create(List.of(item1, item2));
    }
    
    @Test
    @DisplayName("Should save order successfully")
    void shouldSaveOrderSuccessfully() {
        // When
        Order savedOrder = orderRepository.save(testOrder);
        
        // Then
        assertThat(savedOrder.getId()).isNotNull();
        assertThat(savedOrder.getItems()).hasSize(2);
        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
    }
    
    @Test
    @DisplayName("Should find order by id after saving")
    void shouldFindOrderByIdAfterSaving() {
        // Given
        Order savedOrder = orderRepository.save(testOrder);
        OrderId orderId = savedOrder.getId();
        
        // When
        Optional<Order> foundOrder = orderRepository.findById(orderId);
        
        // Then
        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get().getId()).isEqualTo(orderId);
        assertThat(foundOrder.get().getItems()).hasSize(2);
        assertThat(foundOrder.get().getStatus()).isEqualTo(OrderStatus.PENDING);
    }
    
    @Test
    @DisplayName("Should return empty when order not found")
    void shouldReturnEmptyWhenOrderNotFound() {
        // Given
        OrderId nonExistentId = OrderId.generate();
        
        // When
        Optional<Order> foundOrder = orderRepository.findById(nonExistentId);
        
        // Then
        assertThat(foundOrder).isEmpty();
    }
    
    @Test
    @DisplayName("Should update order status when saving")
    void shouldUpdateOrderStatusWhenSaving() {
        // Given
        Order savedOrder = orderRepository.save(testOrder);
        savedOrder.markAsPaid();
        
        // When
        Order updatedOrder = orderRepository.save(savedOrder);
        
        // Then
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.PAID);
        
        // Verify persistence
        Optional<Order> foundOrder = orderRepository.findById(updatedOrder.getId());
        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get().getStatus()).isEqualTo(OrderStatus.PAID);
    }
    
    @Test
    @DisplayName("Should save order with shipping address")
    void shouldSaveOrderWithShippingAddress() {
        // Given
        Address shippingAddress = new Address(
            "123 Main St", 
            "New York", 
            "NY", 
            "10001", 
            "USA"
        );
        testOrder.setShippingAddress(shippingAddress);
        
        // When
        Order savedOrder = orderRepository.save(testOrder);
        
        // Then
        assertThat(savedOrder.getShippingAddress()).isNotNull();
        assertThat(savedOrder.getShippingAddress().getStreet()).isEqualTo("123 Main St");
        assertThat(savedOrder.getShippingAddress().getCity()).isEqualTo("New York");
        
        // Verify persistence
        Optional<Order> foundOrder = orderRepository.findById(savedOrder.getId());
        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get().getShippingAddress()).isNotNull();
        assertThat(foundOrder.get().getShippingAddress().getStreet()).isEqualTo("123 Main St");
    }
    
    @Test
    @DisplayName("Should delete order successfully")
    void shouldDeleteOrderSuccessfully() {
        // Given
        Order savedOrder = orderRepository.save(testOrder);
        OrderId orderId = savedOrder.getId();
        
        // When
        orderRepository.delete(orderId);
        
        // Then
        Optional<Order> foundOrder = orderRepository.findById(orderId);
        assertThat(foundOrder).isEmpty();
    }
    
    @Test
    @DisplayName("Should persist order items correctly")
    void shouldPersistOrderItemsCorrectly() {
        // Given
        Order savedOrder = orderRepository.save(testOrder);
        
        // When
        Optional<Order> foundOrder = orderRepository.findById(savedOrder.getId());
        
        // Then
        assertThat(foundOrder).isPresent();
        List<OrderItem> items = foundOrder.get().getItems();
        assertThat(items).hasSize(2);
        
        OrderItem item1 = items.get(0);
        assertThat(item1.getProductId()).isIn("PROD-001", "PROD-002");
        assertThat(item1.getQuantity()).isPositive();
        assertThat(item1.getUnitPrice()).isNotNull();
    }
    
    @Test
    @DisplayName("Should calculate total amount correctly after persistence")
    void shouldCalculateTotalAmountCorrectlyAfterPersistence() {
        // Given
        Order savedOrder = orderRepository.save(testOrder);
        
        // When
        Optional<Order> foundOrder = orderRepository.findById(savedOrder.getId());
        
        // Then
        assertThat(foundOrder).isPresent();
        Money totalAmount = foundOrder.get().getTotalAmount();
        assertThat(totalAmount.getAmount()).isEqualByComparingTo(new BigDecimal("35.00"));
        assertThat(totalAmount.getCurrency()).isEqualTo(Currency.getInstance("USD"));
    }
}
