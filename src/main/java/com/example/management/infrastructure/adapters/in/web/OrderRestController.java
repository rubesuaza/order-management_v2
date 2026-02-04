package com.example.management.infrastructure.adapters.in.web;

import com.example.management.application.ports.in.ManageOrdersPort;
import com.example.management.infrastructure.adapters.in.web.dto.CreateOrderRequest;
import com.example.management.infrastructure.adapters.in.web.dto.OrderResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST input adapter for order management. Maps HTTP to use cases and returns DTOs.
 */
@RestController
@RequestMapping("/orders")
public class OrderRestController {

    private final ManageOrdersPort manageOrders;

    public OrderRestController(ManageOrdersPort manageOrders) {
        this.manageOrders = manageOrders;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        List<ManageOrdersPort.OrderItemCommand> items = request.items().stream()
                .map(dto -> new ManageOrdersPort.OrderItemCommand(
                        dto.productId(),
                        dto.quantity(),
                        dto.unitPriceAmount(),
                        dto.unitPriceCurrency()))
                .toList();
        var order = manageOrders.createOrder(request.customerId(), items);
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.from(order));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID id) {
        return manageOrders.getOrder(id)
                .map(order -> ResponseEntity.ok(OrderResponse.from(order)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<OrderResponse> listOrders() {
        return manageOrders.listOrders().stream()
                .map(OrderResponse::from)
                .toList();
    }

    @PostMapping("/{id}/mark-paid")
    public ResponseEntity<Void> markAsPaid(@PathVariable UUID id) {
        manageOrders.markOrderAsPaid(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/mark-shipped")
    public ResponseEntity<Void> markAsShipped(@PathVariable UUID id) {
        manageOrders.markOrderAsShipped(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable UUID id) {
        manageOrders.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }
}
