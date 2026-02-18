package com.example.order_management.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Address Value Object Tests")
class AddressTest {

    @Test
    @DisplayName("Should create Address with all fields")
    void shouldCreateAddressWithAllFields() {
        // Given
        String street = "123 Main St";
        String city = "New York";
        String state = "NY";
        String zipCode = "10001";
        String country = "USA";

        // When
        Address address = new Address(street, city, state, zipCode, country);

        // Then
        assertThat(address.getStreet()).isEqualTo(street);
        assertThat(address.getCity()).isEqualTo(city);
        assertThat(address.getState()).isEqualTo(state);
        assertThat(address.getZipCode()).isEqualTo(zipCode);
        assertThat(address.getCountry()).isEqualTo(country);
    }

    @Test
    @DisplayName("Should be equal when all fields are the same")
    void shouldBeEqualWhenAllFieldsAreSame() {
        // Given
        Address address1 = new Address("123 Main St", "New York", "NY", "10001", "USA");
        Address address2 = new Address("123 Main St", "New York", "NY", "10001", "USA");

        // When & Then
        assertThat(address1).isEqualTo(address2);
        assertThat(address1.hashCode()).isEqualTo(address2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when fields differ")
    void shouldNotBeEqualWhenFieldsDiffer() {
        // Given
        Address address1 = new Address("123 Main St", "New York", "NY", "10001", "USA");
        Address address2 = new Address("456 Oak Ave", "New York", "NY", "10001", "USA");

        // When & Then
        assertThat(address1).isNotEqualTo(address2);
    }
}
