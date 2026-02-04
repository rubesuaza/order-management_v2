package com.example.management.infrastructure.adapters.in.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for an order line item in API requests/responses.
 */
public record OrderItemDto(UUID productId, int quantity, BigDecimal unitPriceAmount, String unitPriceCurrency) {

    public OrderItemDto {
        if (unitPriceCurrency == null || unitPriceCurrency.isBlank()) {
            unitPriceCurrency = "USD";
        }
    }
}
