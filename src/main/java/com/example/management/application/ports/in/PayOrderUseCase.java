package com.example.management.application.ports.in;

import java.util.UUID;

public interface PayOrderUseCase {

    PayOrderResponse payOrder(UUID orderId);

    record PayOrderResponse(UUID orderId, String status) {
    }
}

