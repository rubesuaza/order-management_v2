package com.example.order_management.infrastructure.adapters.out.persistence;

import com.example.order_management.domain.Order;
import com.example.order_management.domain.OrderFactory;
import com.example.order_management.domain.OrderItem;
import com.example.order_management.domain.OrderStatus;
import com.example.order_management.domain.port.OrderRepository;
import com.example.order_management.domain.valueobject.Money;
import com.example.order_management.domain.valueobject.OrderId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Contract/Integration tests for OrderRepository adapter. Verifies persistence contract with H2.
 */
@DataJpaTest
@ActiveProfiles("h2")
@Import(OrderRepositoryAdapter.class)
class OrderRepositoryAdapterIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("save persists order and returns it")
    void save_persistsOrderAndReturnsIt() {
        OrderId id = OrderId.generate();
        List<OrderItem> items = List.of(
                OrderItem.create("prod-1", Money.of(new BigDecimal("5.00"), "USD"), 2)
        );
        Order order = OrderFactory.create(id, items);

        Order saved = orderRepository.save(order);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isEqualTo(id);
        assertThat(saved.getItems()).hasSize(1);
        assertThat(saved.getTotalAmount().getAmount()).isEqualByComparingTo(new BigDecimal("10.00"));
    }

    @Test
    @DisplayName("findById returns empty when order does not exist")
    void findById_returnsEmptyWhenNotExists() {
        Optional<Order> found = orderRepository.findById(OrderId.of(UUID.randomUUID()));
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("findById returns saved order")
    void findById_returnsSavedOrder() {
        OrderId id = OrderId.generate();
        List<OrderItem> items = List.of(
                OrderItem.create("prod-2", Money.of(new BigDecimal("15.50"), "USD"), 1)
        );
        Order order = OrderFactory.create(id, items);
        orderRepository.save(order);

        Optional<Order> found = orderRepository.findById(id);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(id);
        assertThat(found.get().getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(found.get().getTotalAmount().getAmount()).isEqualByComparingTo(new BigDecimal("15.50"));
    }

    @Test
    @DisplayName("saved order can be updated and found with new status")
    void save_updateAndFind_reflectsStatus() {
        OrderId id = OrderId.generate();
        List<OrderItem> items = List.of(
                OrderItem.create("prod-3", Money.of(new BigDecimal("20.00"), "USD"), 1)
        );
        Order order = OrderFactory.create(id, items);
        orderRepository.save(order);
        order.markAsPaid();
        orderRepository.save(order);

        Optional<Order> found = orderRepository.findById(id);
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(OrderStatus.PAID);
    }
}
