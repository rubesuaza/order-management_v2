package com.example.order_management.domain.model;

import com.example.order_management.domain.exception.InvalidOrderStateException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Order Aggregate Root Tests")
class OrderTest {

    @Test
    @DisplayName("Should create Order with at least one item")
    void shouldCreateOrderWithAtLeastOneItem() {
        // Given
        OrderItem item = new OrderItem("PROD-001", 2, 
                new Money(new BigDecimal("15.00"), Currency.getInstance("USD")));

        // When
        Order order = Order.create(List.of(item));

        // Then
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getId()).isNotNull();
    }

    @Test
    @DisplayName("Should throw exception when creating Order with empty items")
    void shouldThrowExceptionWhenCreatingOrderWithEmptyItems() {
        // When & Then
        assertThatThrownBy(() -> Order.create(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Order must have at least one item");
    }

    @Test
    @DisplayName("Should calculate total amount correctly")
    void shouldCalculateTotalAmountCorrectly() {
        // Given
        OrderItem item1 = new OrderItem("PROD-001", 2, 
                new Money(new BigDecimal("10.00"), Currency.getInstance("USD")));
        OrderItem item2 = new OrderItem("PROD-002", 3, 
                new Money(new BigDecimal("5.00"), Currency.getInstance("USD")));
        Order order = Order.create(List.of(item1, item2));

        // When
        Money totalAmount = order.getTotalAmount();

        // Then
        assertThat(totalAmount.getAmount()).isEqualByComparingTo(new BigDecimal("35.00"));
    }

    @Test
    @DisplayName("Should transition from PENDING to PAID")
    void shouldTransitionFromPendingToPaid() {
        // Given
        Order order = Order.create(List.of(
                new OrderItem("PROD-001", 1, 
                        new Money(new BigDecimal("15.00"), Currency.getInstance("USD")))
        ));

        // When
        order.markAsPaid();

        // Then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    @DisplayName("Should throw exception when marking as PAID with total less than 10.00")
    void shouldThrowExceptionWhenMarkingAsPaidWithTotalLessThanTen() {
        // Given
        Order order = Order.create(List.of(
                new OrderItem("PROD-001", 1, 
                        new Money(new BigDecimal("5.00"), Currency.getInstance("USD")))
        ));

        // When & Then
        assertThatThrownBy(() -> order.markAsPaid())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Order total must be at least 10.00");
    }

    @Test
    @DisplayName("Should transition from PAID to SHIPPED")
    void shouldTransitionFromPaidToShipped() {
        // Given
        Order order = Order.create(List.of(
                new OrderItem("PROD-001", 1, 
                        new Money(new BigDecimal("15.00"), Currency.getInstance("USD")))
        ));
        order.markAsPaid();

        // When
        order.markAsShipped();

        // Then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    @DisplayName("Should throw exception when shipping from non-PAID status")
    void shouldThrowExceptionWhenShippingFromNonPaidStatus() {
        // Given
        Order order = Order.create(List.of(
                new OrderItem("PROD-001", 1, 
                        new Money(new BigDecimal("15.00"), Currency.getInstance("USD")))
        ));

        // When & Then
        assertThatThrownBy(() -> order.markAsShipped())
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("Order must be PAID before shipping");
    }

    @Test
    @DisplayName("Should transition from PENDING to CANCELLED")
    void shouldTransitionFromPendingToCancelled() {
        // Given
        Order order = Order.create(List.of(
                new OrderItem("PROD-001", 1, 
                        new Money(new BigDecimal("15.00"), Currency.getInstance("USD")))
        ));

        // When
        order.cancel();

        // Then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("Should transition from PAID to CANCELLED")
    void shouldTransitionFromPaidToCancelled() {
        // Given
        Order order = Order.create(List.of(
                new OrderItem("PROD-001", 1, 
                        new Money(new BigDecimal("15.00"), Currency.getInstance("USD")))
        ));
        order.markAsPaid();

        // When
        order.cancel();

        // Then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("Should throw exception when cancelling SHIPPED order")
    void shouldThrowExceptionWhenCancellingShippedOrder() {
        // Given
        Order order = Order.create(List.of(
                new OrderItem("PROD-001", 1, 
                        new Money(new BigDecimal("15.00"), Currency.getInstance("USD")))
        ));
        order.markAsPaid();
        order.markAsShipped();

        // When & Then
        assertThatThrownBy(() -> order.cancel())
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("Cannot cancel order in status SHIPPED");
    }

    @Test
    @DisplayName("Should set shipping address")
    void shouldSetShippingAddress() {
        // Given
        Order order = Order.create(List.of(
                new OrderItem("PROD-001", 1, 
                        new Money(new BigDecimal("15.00"), Currency.getInstance("USD")))
        ));
        Address shippingAddress = new Address("123 Main St", "New York", "NY", "10001", "USA");

        // When
        order.setShippingAddress(shippingAddress);

        // Then
        assertThat(order.getShippingAddress()).isEqualTo(shippingAddress);
    }
}
