package com.example.management.order.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DomainExceptionsTest {

    @Test
    @DisplayName("Las excepciones de dominio deben extender DomainException")
    void hierarchy() {
        DomainException invalidState = new InvalidOrderStateException("msg");
        DomainException invalidItem = new InvalidItemException("msg");
        DomainException currencyMismatch = new CurrencyMismatchException("msg");

        assertThat(invalidState).isInstanceOf(DomainException.class);
        assertThat(invalidItem).isInstanceOf(DomainException.class);
        assertThat(currencyMismatch).isInstanceOf(DomainException.class);
    }
}

