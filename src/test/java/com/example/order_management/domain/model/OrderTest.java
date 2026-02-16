package com.example.order_management.domain.model;

import com.example.order_management.domain.exception.DomainException;
import com.example.order_management.domain.exception.InvalidOrderStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Order Aggregate Root Tests")
class OrderTest {

    private final UUID customerId = UUID.randomUUID();
    private final UUID productId1 = UUID.randomUUID();
    private final UUID productId2 = UUID.randomUUID();

    @Test
    @DisplayName("Should create Order with at least one item")
    void shouldCreateOrderWithAtLeastOneItem() {
        List<OrderItem> items = List.of(
            new OrderItem(productId1, 2, new Money(new BigDecimal("15.00")))
        );
        
        Order order = new Order(UUID.randomUUID(), customerId, items);
        
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getCustomerId()).isEqualTo(customerId);
        assertThat(order.getTotalAmount().getAmount()).isEqualByComparingTo(new BigDecimal("30.00"));
    }

    @Test
    @DisplayName("Should throw exception when creating Order with empty items")
    void shouldThrowExceptionWhenCreatingOrderWithEmptyItems() {
        assertThatThrownBy(() -> new Order(UUID.randomUUID(), customerId, new ArrayList<>()))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("at least one");
    }

    @Test
    @DisplayName("Should calculate total amount correctly for multiple items")
    void shouldCalculateTotalAmountCorrectly() {
        List<OrderItem> items = List.of(
            new OrderItem(productId1, 2, new Money(new BigDecimal("10.00"))),
            new OrderItem(productId2, 3, new Money(new BigDecimal("5.00")))
        );
        
        Order order = new Order(UUID.randomUUID(), customerId, items);
        
        assertThat(order.getTotalAmount().getAmount()).isEqualByComparingTo(new BigDecimal("35.00"));
    }

    @Test
    @DisplayName("Should mark order as PAID when total is at least 10.00 USD")
    void shouldMarkOrderAsPaidWhenTotalIsAtLeastMinimum() {
        List<OrderItem> items = List.of(
            new OrderItem(productId1, 1, new Money(new BigDecimal("10.00")))
        );
        
        Order order = new Order(UUID.randomUUID(), customerId, items);
        order.markAsPaid();
        
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    @DisplayName("Should throw exception when marking as PAID if total is less than 10.00 USD")
    void shouldThrowExceptionWhenMarkingAsPaidBelowMinimum() {
        List<OrderItem> items = List.of(
            new OrderItem(productId1, 1, new Money(new BigDecimal("9.99")))
        );
        
        Order order = new Order(UUID.randomUUID(), customerId, items);
        
        assertThatThrownBy(order::markAsPaid)
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("minimum");
    }

    @Test
    @DisplayName("Should cancel order when status is PENDING")
    void shouldCancelOrderWhenPending() {
        List<OrderItem> items = List.of(
            new OrderItem(productId1, 1, new Money(new BigDecimal("15.00")))
        );
        
        Order order = new Order(UUID.randomUUID(), customerId, items);
        order.cancel();
        
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("Should cancel order when status is PAID")
    void shouldCancelOrderWhenPaid() {
        List<OrderItem> items = List.of(
            new OrderItem(productId1, 1, new Money(new BigDecimal("15.00")))
        );
        
        Order order = new Order(UUID.randomUUID(), customerId, items);
        order.markAsPaid();
        order.cancel();
        
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("Should throw exception when cancelling SHIPPED order")
    void shouldThrowExceptionWhenCancellingShippedOrder() {
        List<OrderItem> items = List.of(
            new OrderItem(productId1, 1, new Money(new BigDecimal("15.00")))
        );
        
        Order order = new Order(UUID.randomUUID(), customerId, items);
        order.markAsPaid();
        order.markAsShipped();
        
        assertThatThrownBy(order::cancel)
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("cancel");
    }

    @Test
    @DisplayName("Should ship order when status is PAID")
    void shouldShipOrderWhenPaid() {
        List<OrderItem> items = List.of(
            new OrderItem(productId1, 1, new Money(new BigDecimal("15.00")))
        );
        
        Order order = new Order(UUID.randomUUID(), customerId, items);
        order.markAsPaid();
        order.markAsShipped();
        
        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    @DisplayName("Should throw exception when shipping order that is not PAID")
    void shouldThrowExceptionWhenShippingNonPaidOrder() {
        List<OrderItem> items = List.of(
            new OrderItem(productId1, 1, new Money(new BigDecimal("15.00")))
        );
        
        Order order = new Order(UUID.randomUUID(), customerId, items);
        
        assertThatThrownBy(order::markAsShipped)
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("PAID");
    }

    @Test
    @DisplayName("Should mark order as DELIVERED when status is SHIPPED")
    void shouldMarkOrderAsDeliveredWhenShipped() {
        List<OrderItem> items = List.of(
            new OrderItem(productId1, 1, new Money(new BigDecimal("15.00")))
        );
        
        Order order = new Order(UUID.randomUUID(), customerId, items);
        order.markAsPaid();
        order.markAsShipped();
        order.markAsDelivered();
        
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    @DisplayName("Should have createdAt timestamp when created")
    void shouldHaveCreatedAtTimestamp() {
        List<OrderItem> items = List.of(
            new OrderItem(productId1, 1, new Money(new BigDecimal("15.00")))
        );
        
        LocalDateTime beforeCreation = LocalDateTime.now();
        Order order = new Order(UUID.randomUUID(), customerId, items);
        LocalDateTime afterCreation = LocalDateTime.now();
        
        assertThat(order.getCreatedAt())
                .isAfterOrEqualTo(beforeCreation)
                .isBeforeOrEqualTo(afterCreation);
    }
}
