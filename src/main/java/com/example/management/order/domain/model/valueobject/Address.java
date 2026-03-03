package com.example.management.order.domain.model.valueobject;

import java.util.Objects;

public final class Address {

    private final String street;
    private final String city;
    private final String zipCode;
    private final String country;

    public Address(String street, String city, String zipCode, String country) {
        this.street = Objects.requireNonNull(street, "street must not be null");
        this.city = Objects.requireNonNull(city, "city must not be null");
        this.zipCode = Objects.requireNonNull(zipCode, "zipCode must not be null");
        this.country = Objects.requireNonNull(country, "country must not be null");
    }

    public String street() {
        return street;
    }

    public String city() {
        return city;
    }

    public String zipCode() {
        return zipCode;
    }

    public String country() {
        return country;
    }
}

