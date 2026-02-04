package com.example.management.application.ports.in;

import com.example.management.domain.model.Order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Input port for order management use cases. Application service implements this interface.
 */
public interface ManageOrdersPort {

    Order createOrder(UUID customerId, List<OrderItemCommand> items);

    Optional<Order> getOrder(UUID orderId);

    List<Order> listOrders();

    void markOrderAsPaid(UUID orderId);

    void markOrderAsShipped(UUID orderId);

    void cancelOrder(UUID orderId);

    /**
     * Command for a single order item (productId, quantity, unitPrice amount and currency).
     */
    record OrderItemCommand(UUID productId, int quantity, java.math.BigDecimal unitPriceAmount, String unitPriceCurrency) {}
}
