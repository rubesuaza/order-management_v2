package com.example.management.infrastructure.adapters.in.web.dto;

import com.example.management.domain.model.Order;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * API response for an order.
 */
public record OrderResponse(
        UUID id,
        UUID customerId,
        String status,
        List<OrderItemDto> items,
        BigDecimal totalAmount,
        String totalCurrency
) {

    public static OrderResponse from(Order order) {
        List<OrderItemDto> items = order.getItems().stream()
                .map(item -> new OrderItemDto(
                        item.getProductId(),
                        item.getQuantity(),
                        item.getUnitPrice().getAmount(),
                        item.getUnitPrice().getCurrency()))
                .toList();
        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getStatus().name(),
                items,
                order.getTotalAmount().getAmount(),
                order.getTotalAmount().getCurrency()
        );
    }
}
