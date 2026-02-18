package com.example.order_management.domain.port;

import com.example.order_management.domain.valueobject.Money;

/**
 * Port for processing payments (optional/future). Implemented by infrastructure.
 */
public interface PaymentGatewayPort {

    /**
     * Request payment for the given amount. Implementation may throw on failure.
     */
    void charge(Money amount);
}
