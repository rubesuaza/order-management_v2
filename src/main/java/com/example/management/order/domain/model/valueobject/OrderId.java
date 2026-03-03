package com.example.management.order.domain.model.valueobject;

import java.util.Objects;
import java.util.UUID;

public final class OrderId {

    private final UUID value;

    private OrderId(UUID value) {
        this.value = value;
    }

    public static OrderId of(UUID value) {
        Objects.requireNonNull(value, "OrderId value must not be null");
        return new OrderId(value);
    }

    public UUID value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderId orderId)) return false;
        return value.equals(orderId.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}

