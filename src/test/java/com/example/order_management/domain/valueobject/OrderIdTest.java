package com.example.order_management.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for OrderId value object.
 */
@DisplayName("OrderId")
class OrderIdTest {

    @Test
    @DisplayName("of wraps given UUID")
    void ofWrapsGivenUuid() {
        UUID uuid = UUID.randomUUID();
        OrderId id = OrderId.of(uuid);
        assertThat(id.getValue()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("of throws when value is null")
    void ofThrowsWhenNull() {
        assertThatThrownBy(() -> OrderId.of(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("value");
    }

    @Test
    @DisplayName("generate creates new id with random UUID")
    void generateCreatesNewId() {
        OrderId id1 = OrderId.generate();
        OrderId id2 = OrderId.generate();
        assertThat(id1).isNotNull();
        assertThat(id2).isNotNull();
        assertThat(id1.getValue()).isNotEqualTo(id2.getValue());
    }

    @Test
    @DisplayName("equals by value")
    void equalsByValue() {
        UUID uuid = UUID.randomUUID();
        OrderId a = OrderId.of(uuid);
        OrderId b = OrderId.of(uuid);
        assertThat(a).isEqualTo(b);
        assertThat(a).hasSameHashCodeAs(b);
    }

    @Test
    @DisplayName("not equal when different UUID")
    void notEqualWhenDifferentUuid() {
        OrderId a = OrderId.of(UUID.randomUUID());
        OrderId b = OrderId.of(UUID.randomUUID());
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    @DisplayName("toString returns UUID string")
    void toStringReturnsUuidString() {
        UUID uuid = UUID.randomUUID();
        OrderId id = OrderId.of(uuid);
        assertThat(id.toString()).isEqualTo(uuid.toString());
    }
}
