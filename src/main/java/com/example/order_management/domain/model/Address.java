package com.example.order_management.domain.model;

import lombok.Value;

/**
 * Value Object representing a physical address.
 * Immutable.
 */
@Value
public class Address {
    
    String street;
    String city;
    String zipCode;
    String country;
    
    public Address(String street, String city, String zipCode, String country) {
        if (street == null || street.isBlank()) {
            throw new IllegalArgumentException("Street cannot be null or blank");
        }
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("City cannot be null or blank");
        }
        if (zipCode == null || zipCode.isBlank()) {
            throw new IllegalArgumentException("Zip code cannot be null or blank");
        }
        if (country == null || country.isBlank()) {
            throw new IllegalArgumentException("Country cannot be null or blank");
        }
        this.street = street;
        this.city = city;
        this.zipCode = zipCode;
        this.country = country;
    }
}
