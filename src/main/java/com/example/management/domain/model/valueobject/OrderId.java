package com.example.management.domain.model.valueobject;

import java.util.Objects;
import java.util.UUID;

public final class OrderId {

    private final UUID value;

    private OrderId(UUID value) {
        this.value = value;
    }

    public static OrderId newId() {
        return new OrderId(UUID.randomUUID());
    }

    public static OrderId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("OrderId value must not be null");
        }
        return new OrderId(value);
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderId orderId = (OrderId) o;
        return Objects.equals(value, orderId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}

