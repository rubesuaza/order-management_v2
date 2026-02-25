package com.example.management.domain.model;

import com.example.management.domain.exception.InvalidItemException;
import com.example.management.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OrderItem")
class OrderItemTest {

    private static final UUID PRODUCT_ID = UUID.randomUUID();

    @Nested
    @DisplayName("creation")
    class Creation {
        @Test
        void createsWithPositiveQuantityAndNonNegativePrice() {
            Money unitPrice = new Money(new BigDecimal("5.00"));
            OrderItem item = new OrderItem(PRODUCT_ID, 2, unitPrice);
            assertThat(item.getProductId()).isEqualTo(PRODUCT_ID);
            assertThat(item.getQuantity()).isEqualTo(2);
            assertThat(item.getUnitPrice()).isEqualTo(unitPrice);
        }

        @Test
        void rejectsZeroQuantity() {
            Money unitPrice = new Money(new BigDecimal("5.00"));
            assertThatThrownBy(() -> new OrderItem(PRODUCT_ID, 0, unitPrice))
                    .isInstanceOf(InvalidItemException.class)
                    .hasMessageContaining("quantity");
        }

        @Test
        void rejectsNegativeQuantity() {
            Money unitPrice = new Money(new BigDecimal("5.00"));
            assertThatThrownBy(() -> new OrderItem(PRODUCT_ID, -1, unitPrice))
                    .isInstanceOf(InvalidItemException.class);
        }

        @Test
        void rejectsNegativeUnitPrice() {
            // Money constructor throws IllegalArgumentException for negative amount before OrderItem runs
            assertThatThrownBy(() -> new OrderItem(PRODUCT_ID, 1, new Money(new BigDecimal("-0.01"))))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Amount");
        }

        @Test
        void acceptsZeroUnitPrice() {
            OrderItem item = new OrderItem(PRODUCT_ID, 1, new Money(BigDecimal.ZERO));
            assertThat(item.getUnitPrice().getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("line total")
    class LineTotal {
        @Test
        void lineTotalIsUnitPriceTimesQuantity() {
            Money unitPrice = new Money(new BigDecimal("3.50"));
            OrderItem item = new OrderItem(PRODUCT_ID, 4, unitPrice);
            Money total = item.getLineTotal();
            assertThat(total.getAmount()).isEqualByComparingTo(new BigDecimal("14.00"));
            assertThat(total.getCurrency()).isEqualTo("USD");
        }
    }
}
