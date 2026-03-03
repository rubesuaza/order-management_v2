package com.example.management.domain.model.valueobject;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que encapsula el identificador de la orden.
 */
public record OrderId(UUID value) {

    public OrderId {
        Objects.requireNonNull(value, "value must not be null");
    }
}

