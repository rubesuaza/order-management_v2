package com.example.order_management.domain.valueobject;

import java.util.Objects;

/**
 * Immutable value object for shipping/billing addresses.
 */
public final class Address {

    private final String street;
    private final String city;
    private final String zipCode;
    private final String country;

    private Address(String street, String city, String zipCode, String country) {
        this.street = Objects.requireNonNull(street, "street");
        this.city = Objects.requireNonNull(city, "city");
        this.zipCode = Objects.requireNonNull(zipCode, "zipCode");
        this.country = Objects.requireNonNull(country, "country");
    }

    public static Address of(String street, String city, String zipCode, String country) {
        return new Address(street, city, zipCode, country);
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getCountry() {
        return country;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(street, address.street)
                && Objects.equals(city, address.city)
                && Objects.equals(zipCode, address.zipCode)
                && Objects.equals(country, address.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(street, city, zipCode, country);
    }
}
