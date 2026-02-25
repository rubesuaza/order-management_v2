package com.example.management.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Address Value Object")
class AddressTest {

    @Test
    void holdsStreetCityZipCodeCountry() {
        Address a = new Address("123 Main St", "New York", "10001", "USA");
        assertThat(a.getStreet()).isEqualTo("123 Main St");
        assertThat(a.getCity()).isEqualTo("New York");
        assertThat(a.getZipCode()).isEqualTo("10001");
        assertThat(a.getCountry()).isEqualTo("USA");
    }

    @Test
    void equalsByAllFields() {
        Address a = new Address("123 Main St", "NY", "10001", "USA");
        Address b = new Address("123 Main St", "NY", "10001", "USA");
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}
