package com.example.management.domain.model.valueobject;

import java.util.Objects;

/**
 * Value Object para la dirección de entrega.
 */
public record Address(String street, String city, String zipCode, String country) {

    public Address {
        Objects.requireNonNull(street, "street must not be null");
        Objects.requireNonNull(city, "city must not be null");
        Objects.requireNonNull(zipCode, "zipCode must not be null");
        Objects.requireNonNull(country, "country must not be null");
    }
}

