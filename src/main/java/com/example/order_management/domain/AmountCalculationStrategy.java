package com.example.order_management.domain;

import com.example.order_management.domain.valueobject.Money;

import java.util.List;

/**
 * Strategy for computing order total (allows future tax/discount rules).
 */
public interface AmountCalculationStrategy {

    Money calculateTotal(List<OrderItem> items);
}
