package com.example.management.infrastructure.adapters.in.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request body for creating an order.
 */
public record CreateOrderRequest(
        @NotNull(message = "customerId is required") java.util.UUID customerId,
        @NotEmpty(message = "At least one item is required") @Valid List<OrderItemDto> items
) {}
