package com.example.management.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Address Value Object")
class AddressTest {

    @Test
    void createsWithAllAttributes() {
        Address address = new Address("Main St 1", "New York", "10001", "USA");
        assertThat(address.getStreet()).isEqualTo("Main St 1");
        assertThat(address.getCity()).isEqualTo("New York");
        assertThat(address.getZipCode()).isEqualTo("10001");
        assertThat(address.getCountry()).isEqualTo("USA");
    }

    @Test
    void equalsByValue() {
        Address a = new Address("St 1", "City", "ZIP", "Country");
        Address b = new Address("St 1", "City", "ZIP", "Country");
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}
