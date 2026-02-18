package com.example.order_management.domain;

import com.example.order_management.domain.valueobject.Address;
import com.example.order_management.domain.valueobject.Money;
import com.example.order_management.domain.valueobject.OrderId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for OrderFactory. create and fromPersistence validations.
 */
@DisplayName("OrderFactory")
class OrderFactoryTest {

    private static OrderId orderId() {
        return OrderId.of(UUID.randomUUID());
    }

    private static List<OrderItem> oneItem() {
        return List.of(OrderItem.create("SKU-1", Money.of(new BigDecimal("10.00"), "USD"), 1));
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("creates order with valid id and items")
        void createsWithValidIdAndItems() {
            OrderId id = orderId();
            List<OrderItem> items = oneItem();

            Order order = OrderFactory.create(id, items);

            assertThat(order).isNotNull();
            assertThat(order.getId()).isEqualTo(id);
            assertThat(order.getItems()).isEqualTo(items);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        }

        @Test
        @DisplayName("throws when id is null")
        void throwsWhenIdIsNull() {
            assertThatThrownBy(() -> OrderFactory.create(null, oneItem()))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("id");
        }

        @Test
        @DisplayName("throws when items is null")
        void throwsWhenItemsIsNull() {
            assertThatThrownBy(() -> OrderFactory.create(orderId(), null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("items");
        }

        @Test
        @DisplayName("throws when items is empty")
        void throwsWhenItemsIsEmpty() {
            assertThatThrownBy(() -> OrderFactory.create(orderId(), List.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("at least one item");
        }
    }

    @Nested
    @DisplayName("fromPersistence")
    class FromPersistence {

        @Test
        @DisplayName("reconstitutes order with status and optional shipping address")
        void reconstitutesWithStatusAndAddress() {
            OrderId id = orderId();
            List<OrderItem> items = oneItem();
            Address address = Address.of("Street", "City", "ZIP", "Country");

            Order order = OrderFactory.fromPersistence(id, items, OrderStatus.SHIPPED, address);

            assertThat(order).isNotNull();
            assertThat(order.getId()).isEqualTo(id);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
            assertThat(order.getShippingAddress()).isEqualTo(address);
        }

        @Test
        @DisplayName("reconstitutes with null shipping address")
        void reconstitutesWithNullAddress() {
            Order order = OrderFactory.fromPersistence(orderId(), oneItem(), OrderStatus.PAID, null);
            assertThat(order.getShippingAddress()).isNull();
        }

        @Test
        @DisplayName("throws when id is null")
        void throwsWhenIdIsNull() {
            assertThatThrownBy(() -> OrderFactory.fromPersistence(null, oneItem(), OrderStatus.PENDING, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("id");
        }

        @Test
        @DisplayName("throws when items is null")
        void throwsWhenItemsIsNull() {
            assertThatThrownBy(() -> OrderFactory.fromPersistence(orderId(), null, OrderStatus.PENDING, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("items");
        }

        @Test
        @DisplayName("throws when status is null")
        void throwsWhenStatusIsNull() {
            assertThatThrownBy(() -> OrderFactory.fromPersistence(orderId(), oneItem(), null, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("status");
        }

        @Test
        @DisplayName("throws when items is empty")
        void throwsWhenItemsIsEmpty() {
            assertThatThrownBy(() -> OrderFactory.fromPersistence(orderId(), List.of(), OrderStatus.PENDING, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("at least one item");
        }
    }
}
