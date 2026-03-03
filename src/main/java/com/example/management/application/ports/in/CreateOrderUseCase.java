package com.example.management.application.ports.in;

import java.util.List;
import java.util.UUID;

public interface CreateOrderUseCase {

    CreateOrderResponse createOrder(CreateOrderCommand command);

    record CreateOrderCommand(UUID customerId, List<OrderItemCommand> items) {
    }

    record OrderItemCommand(UUID productId, int quantity, java.math.BigDecimal unitPrice) {
    }

    record CreateOrderResponse(UUID orderId, String status, java.math.BigDecimal totalAmount, String currency, java.time.OffsetDateTime createdAt) {
    }
}

