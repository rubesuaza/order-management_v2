package com.example.management.application.services;

import com.example.management.application.ports.in.ManageOrdersPort;
import com.example.management.application.ports.out.OrderRepository;
import com.example.management.domain.model.Money;
import com.example.management.domain.model.Order;
import com.example.management.domain.model.OrderItem;
import com.example.management.domain.model.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderManagementService")
class OrderManagementServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    private OrderManagementService sut;

    private UUID customerId;
    private UUID productId;
    private Order existingOrder;

    @BeforeEach
    void setUp() {
        sut = new OrderManagementService(orderRepository);
        customerId = UUID.randomUUID();
        productId = UUID.randomUUID();
        OrderItem item = new OrderItem(productId, 2, new Money(new BigDecimal("10.00")));
        existingOrder = new Order(UUID.randomUUID(), customerId, LocalDateTime.now(), List.of(item));
    }

    @Nested
    @DisplayName("createOrder")
    class CreateOrder {
        @Test
        void createsOrderWithItemsAndSavesToRepository() {
            var items = List.of(
                    new ManageOrdersPort.OrderItemCommand(productId, 2, new BigDecimal("15.50"), "USD")
            );
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            Order result = sut.createOrder(customerId, items);

            verify(orderRepository).save(orderCaptor.capture());
            Order saved = orderCaptor.getValue();
            assertThat(saved.getCustomerId()).isEqualTo(customerId);
            assertThat(saved.getItems()).hasSize(1);
            assertThat(saved.getItems().get(0).getProductId()).isEqualTo(productId);
            assertThat(saved.getItems().get(0).getQuantity()).isEqualTo(2);
            assertThat(saved.getItems().get(0).getUnitPrice().getAmount()).isEqualByComparingTo("15.50");
            assertThat(saved.getItems().get(0).getUnitPrice().getCurrency()).isEqualTo("USD");
            assertThat(saved.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(result).isSameAs(saved);
        }

        @Test
        void usesUsdWhenCurrencyIsNull() {
            var items = List.of(
                    new ManageOrdersPort.OrderItemCommand(productId, 1, new BigDecimal("99.00"), null)
            );
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            sut.createOrder(customerId, items);

            verify(orderRepository).save(orderCaptor.capture());
            assertThat(orderCaptor.getValue().getItems().get(0).getUnitPrice().getCurrency()).isEqualTo("USD");
        }

        @Test
        void mapsMultipleItemsCorrectly() {
            UUID product2 = UUID.randomUUID();
            var items = List.of(
                    new ManageOrdersPort.OrderItemCommand(productId, 1, new BigDecimal("10.00"), "EUR"),
                    new ManageOrdersPort.OrderItemCommand(product2, 3, new BigDecimal("5.00"), "EUR")
            );
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            sut.createOrder(customerId, items);

            verify(orderRepository).save(orderCaptor.capture());
            Order saved = orderCaptor.getValue();
            assertThat(saved.getItems()).hasSize(2);
            assertThat(saved.getItems().get(0).getProductId()).isEqualTo(productId);
            assertThat(saved.getItems().get(0).getQuantity()).isEqualTo(1);
            assertThat(saved.getItems().get(1).getProductId()).isEqualTo(product2);
            assertThat(saved.getItems().get(1).getQuantity()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("getOrder")
    class GetOrder {
        @Test
        void returnsOrderWhenFound() {
            UUID orderId = existingOrder.getId();
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));

            var result = sut.getOrder(orderId);

            assertThat(result).contains(existingOrder);
        }

        @Test
        void returnsEmptyWhenOrderNotFound() {
            UUID orderId = UUID.randomUUID();
            when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

            var result = sut.getOrder(orderId);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("listOrders")
    class ListOrders {
        @Test
        void returnsAllOrdersFromRepository() {
            var orders = List.of(existingOrder);
            when(orderRepository.findAll()).thenReturn(orders);

            var result = sut.listOrders();

            assertThat(result).isEqualTo(orders);
        }

        @Test
        void returnsEmptyListWhenNoOrders() {
            when(orderRepository.findAll()).thenReturn(List.of());

            var result = sut.listOrders();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("markOrderAsPaid")
    class MarkOrderAsPaid {
        @Test
        void marksOrderAsPaidAndSaves() {
            // existingOrder has one item 2x10.00 USD => total 20.00 >= 10.00
            UUID orderId = existingOrder.getId();
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            sut.markOrderAsPaid(orderId);

            verify(orderRepository).save(orderCaptor.capture());
            assertThat(orderCaptor.getValue().getStatus()).isEqualTo(OrderStatus.PAID);
        }

        @Test
        void throwsWhenOrderNotFound() {
            UUID orderId = UUID.randomUUID();
            when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.markOrderAsPaid(orderId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Order not found")
                    .hasMessageContaining(orderId.toString());
        }
    }

    @Nested
    @DisplayName("markOrderAsShipped")
    class MarkOrderAsShipped {
        @Test
        void marksOrderAsShippedAndSaves() {
            existingOrder.markAsPaid(); // need PAID to ship
            UUID orderId = existingOrder.getId();
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            sut.markOrderAsShipped(orderId);

            verify(orderRepository).save(orderCaptor.capture());
            assertThat(orderCaptor.getValue().getStatus()).isEqualTo(OrderStatus.SHIPPED);
        }

        @Test
        void throwsWhenOrderNotFound() {
            UUID orderId = UUID.randomUUID();
            when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.markOrderAsShipped(orderId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Order not found");
        }
    }

    @Nested
    @DisplayName("cancelOrder")
    class CancelOrder {
        @Test
        void cancelsOrderAndSaves() {
            UUID orderId = existingOrder.getId();
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            sut.cancelOrder(orderId);

            verify(orderRepository).save(orderCaptor.capture());
            assertThat(orderCaptor.getValue().getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }

        @Test
        void throwsWhenOrderNotFound() {
            UUID orderId = UUID.randomUUID();
            when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.cancelOrder(orderId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Order not found");
        }
    }
}
