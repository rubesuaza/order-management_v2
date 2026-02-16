package com.example.order_management.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Address Value Object Tests")
class AddressTest {

    @Test
    @DisplayName("Should create Address with all fields")
    void shouldCreateAddressWithAllFields() {
        Address address = new Address("123 Main St", "New York", "10001", "USA");
        
        assertThat(address.getStreet()).isEqualTo("123 Main St");
        assertThat(address.getCity()).isEqualTo("New York");
        assertThat(address.getZipCode()).isEqualTo("10001");
        assertThat(address.getCountry()).isEqualTo("USA");
    }

    @Test
    @DisplayName("Should be equal when all fields are the same")
    void shouldBeEqualWhenAllFieldsAreSame() {
        Address address1 = new Address("123 Main St", "New York", "10001", "USA");
        Address address2 = new Address("123 Main St", "New York", "10001", "USA");
        
        assertThat(address1).isEqualTo(address2);
        assertThat(address1.hashCode()).isEqualTo(address2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when fields differ")
    void shouldNotBeEqualWhenFieldsDiffer() {
        Address address1 = new Address("123 Main St", "New York", "10001", "USA");
        Address address2 = new Address("456 Oak Ave", "New York", "10001", "USA");
        
        assertThat(address1).isNotEqualTo(address2);
    }
}
