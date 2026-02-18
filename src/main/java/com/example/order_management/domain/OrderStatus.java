package com.example.order_management.domain;

/**
 * Lifecycle states of an Order. Transitions: CANCELLED only from PENDING or PAID; SHIPPED only from PAID.
 */
public enum OrderStatus {
    PENDING,
    PAID,
    SHIPPED,
    CANCELLED
}
