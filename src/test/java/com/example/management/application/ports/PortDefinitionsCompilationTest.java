package com.example.management.application.ports;

import com.example.management.application.ports.in.CancelOrderUseCase;
import com.example.management.application.ports.in.CreateOrderUseCase;
import com.example.management.application.ports.in.PayOrderUseCase;
import com.example.management.application.ports.out.OrderRepository;
import com.example.management.domain.model.aggregate.Order;
import com.example.management.domain.model.valueobject.OrderId;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Prueba ligera para garantizar que las interfaces de puertos
 * de aplicación se pueden compilar y usar desde el dominio.
 */
class PortDefinitionsCompilationTest {

    @Test
    void portsShouldBeUsableFromApplicationLayer() {
        CreateOrderUseCase createOrderUseCase = command -> OrderId.newId();
        PayOrderUseCase payOrderUseCase = command -> {};
        CancelOrderUseCase cancelOrderUseCase = command -> {};

        OrderRepository repository = new OrderRepository() {
            @Override
            public Optional<Order> findById(OrderId id) {
                return Optional.empty();
            }

            @Override
            public Order save(Order order) {
                return order;
            }
        };

        assertNotNull(createOrderUseCase);
        assertNotNull(payOrderUseCase);
        assertNotNull(cancelOrderUseCase);
        assertNotNull(repository);
    }
}

