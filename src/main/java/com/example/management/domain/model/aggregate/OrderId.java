package com.example.management.domain.model.aggregate;

import java.util.UUID;

public record OrderId(UUID value) {

    public static OrderId newId() {
        return new OrderId(UUID.randomUUID());
    }
}

