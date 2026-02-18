package com.example.order_management.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class OrderEntity {
    
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private String status;
    
    @Embedded
    private AddressEmbeddable shippingAddress;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItemEntity> items = new ArrayList<>();
    
    protected OrderEntity() {
        // JPA requires no-arg constructor
    }
    
    public OrderEntity(UUID id, String status, AddressEmbeddable shippingAddress) {
        this.id = id;
        this.status = status;
        this.shippingAddress = shippingAddress;
    }
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public AddressEmbeddable getShippingAddress() {
        return shippingAddress;
    }
    
    public void setShippingAddress(AddressEmbeddable shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
    
    public List<OrderItemEntity> getItems() {
        return items;
    }
    
    public void setItems(List<OrderItemEntity> items) {
        this.items = items;
    }
}
