package com.example.management.infrastructure.adapters.out;

import com.example.management.application.ports.out.OrderRepository;
import com.example.management.domain.model.Order;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of OrderRepository. Suitable for tests and simple deployments.
 * Thread-safe via ConcurrentHashMap.
 */
public class InMemoryOrderRepository implements OrderRepository {

    private final Map<UUID, Order> store = new ConcurrentHashMap<>();

    @Override
    public Order save(Order order) {
        store.put(order.getId(), order);
        return order;
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Order> findAll() {
        return List.copyOf(store.values());
    }
}
