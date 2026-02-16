package com.example.order_management.application.services;

import com.example.order_management.application.ports.in.CreateOrderUseCase;
import com.example.order_management.application.ports.out.OrderRepository;
import com.example.order_management.domain.exception.DomainException;
import com.example.order_management.domain.exception.InvalidOrderStateException;
import com.example.order_management.domain.model.Money;
import com.example.order_management.domain.model.Order;
import com.example.order_management.domain.model.OrderItem;
import com.example.order_management.domain.model.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private UUID customerId;
    private UUID productId1;
    private UUID productId2;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        productId1 = UUID.randomUUID();
        productId2 = UUID.randomUUID();
        orderId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should create order successfully")
    void shouldCreateOrderSuccessfully() {
        // Arrange
        List<CreateOrderUseCase.OrderItemRequest> itemRequests = List.of(
            new CreateOrderUseCase.OrderItemRequest(productId1, 2, new BigDecimal("15.00")),
            new CreateOrderUseCase.OrderItemRequest(productId2, 1, new BigDecimal("10.00"))
        );

        Order expectedOrder = new Order(
            orderId,
            customerId,
            List.of(
                new OrderItem(productId1, 2, new Money(new BigDecimal("15.00"))),
                new OrderItem(productId2, 1, new Money(new BigDecimal("10.00")))
            )
        );

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Order result = orderService.createOrder(customerId, itemRequests);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCustomerId()).isEqualTo(customerId);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getTotalAmount().getAmount()).isEqualByComparingTo(new BigDecimal("40.00"));
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Should get order by ID successfully")
    void shouldGetOrderByIdSuccessfully() {
        // Arrange
        Order order = new Order(
            orderId,
            customerId,
            List.of(new OrderItem(productId1, 1, new Money(new BigDecimal("15.00"))))
        );

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act
        Order result = orderService.getOrderById(orderId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(orderId);
        assertThat(result.getCustomerId()).isEqualTo(customerId);
        verify(orderRepository).findById(orderId);
    }

    @Test
    @DisplayName("Should throw exception when order not found")
    void shouldThrowExceptionWhenOrderNotFound() {
        // Arrange
        UUID nonExistentOrderId = UUID.randomUUID();
        when(orderRepository.findById(nonExistentOrderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.getOrderById(nonExistentOrderId))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("Order not found with ID: " + nonExistentOrderId);

        verify(orderRepository).findById(nonExistentOrderId);
    }

    @Test
    @DisplayName("Should pay order successfully")
    void shouldPayOrderSuccessfully() {
        // Arrange
        Order order = new Order(
            orderId,
            customerId,
            List.of(new OrderItem(productId1, 1, new Money(new BigDecimal("15.00"))))
        );

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Order result = orderService.payOrder(orderId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PAID);
        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception when paying non-existent order")
    void shouldThrowExceptionWhenPayingNonExistentOrder() {
        // Arrange
        UUID nonExistentOrderId = UUID.randomUUID();
        when(orderRepository.findById(nonExistentOrderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.payOrder(nonExistentOrderId))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("Order not found with ID: " + nonExistentOrderId);

        verify(orderRepository).findById(nonExistentOrderId);
    }

    @Test
    @DisplayName("Should throw exception when paying order with amount below minimum")
    void shouldThrowExceptionWhenPayingOrderBelowMinimum() {
        // Arrange
        Order order = new Order(
            orderId,
            customerId,
            List.of(new OrderItem(productId1, 1, new Money(new BigDecimal("9.99"))))
        );

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThatThrownBy(() -> orderService.payOrder(orderId))
            .isInstanceOf(InvalidOrderStateException.class)
            .hasMessageContaining("minimum");

        verify(orderRepository).findById(orderId);
    }

    @Test
    @DisplayName("Should create order with single item")
    void shouldCreateOrderWithSingleItem() {
        // Arrange
        List<CreateOrderUseCase.OrderItemRequest> itemRequests = List.of(
            new CreateOrderUseCase.OrderItemRequest(productId1, 3, new BigDecimal("5.00"))
        );

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Order result = orderService.createOrder(customerId, itemRequests);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getProductId()).isEqualTo(productId1);
        assertThat(result.getItems().get(0).getQuantity()).isEqualTo(3);
        assertThat(result.getTotalAmount().getAmount()).isEqualByComparingTo(new BigDecimal("15.00"));
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception when paying order that is not PENDING")
    void shouldThrowExceptionWhenPayingNonPendingOrder() {
        // Arrange
        Order order = new Order(
            orderId,
            customerId,
            List.of(new OrderItem(productId1, 1, new Money(new BigDecimal("15.00"))))
        );
        order.markAsPaid(); // Change status to PAID

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThatThrownBy(() -> orderService.payOrder(orderId))
            .isInstanceOf(InvalidOrderStateException.class)
            .hasMessageContaining("PENDING");

        verify(orderRepository).findById(orderId);
    }
}
