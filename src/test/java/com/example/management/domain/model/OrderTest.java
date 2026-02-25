package com.example.management.domain.model;

import com.example.management.domain.exception.InvalidOrderStateException;
import com.example.management.domain.exception.InvalidItemException;
import com.example.management.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Order Aggregate")
class OrderTest {

    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final UUID PRODUCT_ID = UUID.randomUUID();

    @Nested
    @DisplayName("creation")
    class Creation {
        @Test
        void createsWithAtLeastOneItem() {
            OrderItem item = new OrderItem(PRODUCT_ID, 2, new Money(new BigDecimal("5.00")));
            Order order = new Order(CUSTOMER_ID, List.of(item));
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(order.getItems()).hasSize(1);
            assertThat(order.getTotalAmount().getAmount()).isEqualByComparingTo(new BigDecimal("10.00"));
        }

        @Test
        void rejectsEmptyItems() {
            assertThatThrownBy(() -> new Order(CUSTOMER_ID, List.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("at least one");
        }

        @Test
        void totalAmountIsSumOfLineTotals() {
            OrderItem a = new OrderItem(PRODUCT_ID, 2, new Money(new BigDecimal("5.00")));
            OrderItem b = new OrderItem(UUID.randomUUID(), 1, new Money(new BigDecimal("3.00")));
            Order order = new Order(CUSTOMER_ID, List.of(a, b));
            assertThat(order.getTotalAmount().getAmount()).isEqualByComparingTo(new BigDecimal("13.00"));
        }
    }

    @Nested
    @DisplayName("mark as paid")
    class MarkAsPaid {
        @Test
        void allowsPaidWhenTotalAtLeast10Usd() {
            OrderItem item = new OrderItem(PRODUCT_ID, 2, new Money(new BigDecimal("5.00")));
            Order order = new Order(CUSTOMER_ID, List.of(item));
            order.markAsPaid();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        }

        @Test
        void rejectsPaidWhenTotalLessThan10Usd() {
            OrderItem item = new OrderItem(PRODUCT_ID, 1, new Money(new BigDecimal("9.99")));
            Order order = new Order(CUSTOMER_ID, List.of(item));
            assertThatThrownBy(order::markAsPaid)
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("10");
        }

        @Test
        void rejectsPaidWhenNotPending() {
            OrderItem item = new OrderItem(PRODUCT_ID, 2, new Money(new BigDecimal("10.00")));
            Order order = new Order(CUSTOMER_ID, List.of(item));
            order.markAsPaid();
            assertThatThrownBy(order::markAsPaid)
                    .isInstanceOf(InvalidOrderStateException.class);
        }
    }

    @Nested
    @DisplayName("cancel")
    class Cancel {
        @Test
        void cancelsWhenPending() {
            OrderItem item = new OrderItem(PRODUCT_ID, 1, new Money(new BigDecimal("15.00")));
            Order order = new Order(CUSTOMER_ID, List.of(item));
            order.cancel();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }

        @Test
        void cancelsWhenPaid() {
            OrderItem item = new OrderItem(PRODUCT_ID, 2, new Money(new BigDecimal("10.00")));
            Order order = new Order(CUSTOMER_ID, List.of(item));
            order.markAsPaid();
            order.cancel();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }

        @Test
        void rejectsCancelWhenShipped() {
            OrderItem item = new OrderItem(PRODUCT_ID, 2, new Money(new BigDecimal("10.00")));
            Order order = new Order(CUSTOMER_ID, List.of(item));
            order.markAsPaid();
            order.ship();
            assertThatThrownBy(order::cancel)
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("SHIPPED");
        }

        @Test
        void rejectsCancelWhenAlreadyCancelled() {
            OrderItem item = new OrderItem(PRODUCT_ID, 1, new Money(new BigDecimal("15.00")));
            Order order = new Order(CUSTOMER_ID, List.of(item));
            order.cancel();
            assertThatThrownBy(order::cancel)
                    .isInstanceOf(InvalidOrderStateException.class);
        }
    }

    @Nested
    @DisplayName("ship")
    class Ship {
        @Test
        void shipsWhenPaid() {
            OrderItem item = new OrderItem(PRODUCT_ID, 2, new Money(new BigDecimal("10.00")));
            Order order = new Order(CUSTOMER_ID, List.of(item));
            order.markAsPaid();
            order.ship();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        }

        @Test
        void rejectsShipWhenPending() {
            OrderItem item = new OrderItem(PRODUCT_ID, 2, new Money(new BigDecimal("15.00")));
            Order order = new Order(CUSTOMER_ID, List.of(item));
            assertThatThrownBy(order::ship)
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("PAID");
        }

        @Test
        void rejectsShipWhenAlreadyShipped() {
            OrderItem item = new OrderItem(PRODUCT_ID, 2, new Money(new BigDecimal("10.00")));
            Order order = new Order(CUSTOMER_ID, List.of(item));
            order.markAsPaid();
            order.ship();
            assertThatThrownBy(order::ship)
                    .isInstanceOf(InvalidOrderStateException.class);
        }
    }

    @Nested
    @DisplayName("identity and attributes")
    class Identity {
        @Test
        void hasOrderIdAndCreatedAt() {
            OrderItem item = new OrderItem(PRODUCT_ID, 1, new Money(new BigDecimal("10.00")));
            Order order = new Order(CUSTOMER_ID, List.of(item));
            assertThat(order.getOrderId()).isNotNull();
            assertThat(order.getCustomerId()).isEqualTo(CUSTOMER_ID);
            assertThat(order.getCreatedAt()).isNotNull();
        }
    }
}
