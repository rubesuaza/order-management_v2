package com.example.order_management.application.services;

import com.example.order_management.domain.Order;
import com.example.order_management.domain.OrderFactory;
import com.example.order_management.domain.OrderItem;
import com.example.order_management.domain.OrderStatus;
import com.example.order_management.domain.port.OrderRepository;
import com.example.order_management.domain.port.PaymentGatewayPort;
import com.example.order_management.domain.valueobject.Address;
import com.example.order_management.domain.valueobject.Money;
import com.example.order_management.domain.valueobject.OrderId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for OrderApplicationService. Mocks OrderRepository and PaymentGatewayPort.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderApplicationService")
class OrderApplicationServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentGatewayPort paymentGatewayPort;

    private OrderApplicationService sut;

    private static OrderId orderId() {
        return OrderId.of(UUID.randomUUID());
    }

    private static List<OrderItem> oneItem() {
        return List.of(OrderItem.create("SKU-1", Money.of(new BigDecimal("15.00"), "USD"), 1));
    }

    @BeforeEach
    void setUp() {
        sut = new OrderApplicationService(orderRepository, paymentGatewayPort);
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("creates order via factory and saves to repository")
        void createsAndSavesOrder() {
            OrderId id = orderId();
            List<OrderItem> items = oneItem();
            Order saved = createOrder(id, items);
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            Order result = sut.create(id, items);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(id);
            assertThat(result.getItems()).hasSize(1);
            verify(orderRepository).save(any(Order.class));
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("returns empty when order not found")
        void returnsEmptyWhenNotFound() {
            OrderId id = orderId();
            when(orderRepository.findById(id)).thenReturn(Optional.empty());

            assertThat(sut.getById(id)).isEmpty();
            verify(orderRepository).findById(id);
        }

        @Test
        @DisplayName("returns order when found")
        void returnsOrderWhenFound() {
            OrderId id = orderId();
            Order order = createOrder(id, oneItem());
            when(orderRepository.findById(id)).thenReturn(Optional.of(order));

            assertThat(sut.getById(id)).contains(order);
            verify(orderRepository).findById(id);
        }
    }

    @Nested
    @DisplayName("markAsPaid")
    class MarkAsPaid {

        @Test
        @DisplayName("charges payment gateway and marks order as paid then saves")
        void chargesAndMarksPaidAndSaves() {
            OrderId id = orderId();
            Order order = createOrder(id, oneItem());
            when(orderRepository.findById(id)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            sut.markAsPaid(id);

            ArgumentCaptor<Money> moneyCaptor = ArgumentCaptor.forClass(Money.class);
            verify(paymentGatewayPort).charge(moneyCaptor.capture());
            assertThat(moneyCaptor.getValue().getAmount()).isEqualByComparingTo(new BigDecimal("15.00"));
            verify(orderRepository).save(order);
        }

        @Test
        @DisplayName("throws when order not found")
        void throwsWhenOrderNotFound() {
            OrderId id = orderId();
            when(orderRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.markAsPaid(id))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not found")
                    .hasMessageContaining(id.toString());
            verify(paymentGatewayPort, never()).charge(any());
            verify(orderRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("cancel")
    class Cancel {

        @Test
        @DisplayName("cancels order and saves")
        void cancelsAndSaves() {
            OrderId id = orderId();
            Order order = createOrder(id, oneItem());
            when(orderRepository.findById(id)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            sut.cancel(id);

            verify(orderRepository).save(order);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("throws when order not found")
        void throwsWhenOrderNotFound() {
            OrderId id = orderId();
            when(orderRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.cancel(id))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not found");
            verify(orderRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("ship")
    class Ship {

        @Test
        @DisplayName("ships order with address and saves")
        void shipsAndSaves() {
            OrderId id = orderId();
            Order order = createPaidOrder(id, oneItem());
            Address address = Address.of("Street", "City", "ZIP", "Country");
            when(orderRepository.findById(id)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            sut.ship(id, address);

            verify(orderRepository).save(order);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
            assertThat(order.getShippingAddress()).isEqualTo(address);
        }

        @Test
        @DisplayName("throws when order not found")
        void throwsWhenOrderNotFound() {
            OrderId id = orderId();
            when(orderRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.ship(id, Address.of("S", "C", "Z", "CO")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not found");
            verify(orderRepository, never()).save(any());
        }
    }

    private static Order createOrder(OrderId id, List<OrderItem> items) {
        return OrderFactory.create(id, items);
    }

    private static Order createPaidOrder(OrderId id, List<OrderItem> items) {
        return OrderFactory.fromPersistence(id, items, OrderStatus.PAID, null);
    }
}
