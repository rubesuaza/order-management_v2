package com.example.order_management.domain;

import com.example.order_management.domain.valueobject.Money;

import java.util.List;

/**
 * Default strategy: total = sum of (unitPrice * quantity) per item.
 */
public final class DefaultAmountCalculationStrategy implements AmountCalculationStrategy {

    @Override
    public Money calculateTotal(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("At least one item required");
        }
        Money total = items.get(0).getLineTotal();
        for (int i = 1; i < items.size(); i++) {
            total = total.add(items.get(i).getLineTotal());
        }
        return total;
    }
}
