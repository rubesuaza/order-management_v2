package com.example.management.infrastructure.adapters.out;

import com.example.management.application.ports.out.OrderRepository;
import com.example.management.domain.model.Money;
import com.example.management.domain.model.Order;
import com.example.management.domain.model.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Contract tests for any OrderRepository implementation. InMemoryOrderRepository must pass these.
 */
@DisplayName("OrderRepository contract")
class OrderRepositoryContractTest {

    private OrderRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryOrderRepository();
    }

    @Nested
    @DisplayName("save and findById")
    class SaveAndFindById {

        @Test
        void save_persists_order_and_findById_returns_it() {
            Order order = createSampleOrder();
            Order saved = repository.save(order);
            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isEqualTo(order.getId());

            Optional<Order> found = repository.findById(order.getId());
            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo(order.getId());
            assertThat(found.get().getCustomerId()).isEqualTo(order.getCustomerId());
            assertThat(found.get().getItems()).hasSize(order.getItems().size());
        }

        @Test
        void findById_returns_empty_when_not_found() {
            Optional<Order> found = repository.findById(UUID.randomUUID());
            assertThat(found).isEmpty();
        }

        @Test
        void save_updates_existing_order() {
            Order order = createSampleOrder();
            repository.save(order);
            order.markAsPaid();
            repository.save(order);

            Optional<Order> found = repository.findById(order.getId());
            assertThat(found).isPresent();
            assertThat(found.get().getStatus()).isEqualTo(com.example.management.domain.model.OrderStatus.PAID);
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        void findAll_returns_empty_when_nothing_saved() {
            List<Order> all = repository.findAll();
            assertThat(all).isEmpty();
        }

        @Test
        void findAll_returns_all_saved_orders() {
            Order o1 = createSampleOrder();
            Order o2 = createSampleOrder();
            repository.save(o1);
            repository.save(o2);

            List<Order> all = repository.findAll();
            assertThat(all).hasSize(2);
            assertThat(all).extracting(Order::getId).containsExactlyInAnyOrder(o1.getId(), o2.getId());
        }
    }

    private static Order createSampleOrder() {
        UUID customerId = UUID.randomUUID();
        OrderItem item = new OrderItem(UUID.randomUUID(), 2, new Money(new BigDecimal("15.00"), "USD"));
        return new Order(UUID.randomUUID(), customerId, null, List.of(item));
    }
}
