package com.example.order_management.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for Address value object.
 */
@DisplayName("Address")
class AddressTest {

    @Test
    @DisplayName("of creates address with all fields")
    void ofCreatesAddressWithAllFields() {
        Address address = Address.of("Main St", "Boston", "02101", "USA");
        assertThat(address.getStreet()).isEqualTo("Main St");
        assertThat(address.getCity()).isEqualTo("Boston");
        assertThat(address.getZipCode()).isEqualTo("02101");
        assertThat(address.getCountry()).isEqualTo("USA");
    }

    @Nested
    @DisplayName("null validation")
    class NullValidation {

        @Test
        @DisplayName("throws when street is null")
        void throwsWhenStreetIsNull() {
            assertThatThrownBy(() -> Address.of(null, "City", "ZIP", "Country"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("street");
        }

        @Test
        @DisplayName("throws when city is null")
        void throwsWhenCityIsNull() {
            assertThatThrownBy(() -> Address.of("Street", null, "ZIP", "Country"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("city");
        }

        @Test
        @DisplayName("throws when zipCode is null")
        void throwsWhenZipCodeIsNull() {
            assertThatThrownBy(() -> Address.of("Street", "City", null, "Country"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("zipCode");
        }

        @Test
        @DisplayName("throws when country is null")
        void throwsWhenCountryIsNull() {
            assertThatThrownBy(() -> Address.of("Street", "City", "ZIP", null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("country");
        }
    }

    @Test
    @DisplayName("equals and hashCode by all fields")
    void equalsAndHashCodeByAllFields() {
        Address a = Address.of("S", "C", "Z", "CO");
        Address b = Address.of("S", "C", "Z", "CO");
        Address c = Address.of("Other", "C", "Z", "CO");
        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(c);
    }
}
