package com.example.management.application.ports.in;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface GetOrderQuery {

    OrderDetailsView getOrder(UUID orderId);

    record OrderDetailsView(
            UUID orderId,
            UUID customerId,
            String status,
            List<OrderItemView> items,
            BigDecimal totalAmount,
            String currency
    ) {
    }

    record OrderItemView(
            UUID productId,
            int quantity,
            BigDecimal unitPrice
    ) {
    }
}

