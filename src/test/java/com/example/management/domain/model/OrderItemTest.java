package com.example.management.domain.model;

import com.example.management.domain.exception.InvalidItemException;
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
    private static final Money VALID_PRICE = new Money(new BigDecimal("19.99"));

    @Nested
    @DisplayName("creation")
    class Creation {
        @Test
        void createsWithPositiveQuantityAndNonNegativePrice() {
            OrderItem item = new OrderItem(PRODUCT_ID, 2, VALID_PRICE);
            assertThat(item.getProductId()).isEqualTo(PRODUCT_ID);
            assertThat(item.getQuantity()).isEqualTo(2);
            assertThat(item.getUnitPrice()).isEqualTo(VALID_PRICE);
        }

        @Test
        void quantityMustBeStrictlyPositive() {
            assertThatThrownBy(() -> new OrderItem(PRODUCT_ID, 0, VALID_PRICE))
                    .isInstanceOf(InvalidItemException.class)
                    .hasMessageContaining("Quantity");
            assertThatThrownBy(() -> new OrderItem(PRODUCT_ID, -1, VALID_PRICE))
                    .isInstanceOf(InvalidItemException.class);
        }

        @Test
        void unitPriceCannotBeNegative() {
            Money negativePrice = new Money(new BigDecimal("-5.00"));
            assertThatThrownBy(() -> new OrderItem(PRODUCT_ID, 1, negativePrice))
                    .isInstanceOf(InvalidItemException.class)
                    .hasMessageContaining("price");
        }

        @Test
        void acceptsZeroUnitPrice() {
            Money zeroPrice = new Money(BigDecimal.ZERO);
            OrderItem item = new OrderItem(PRODUCT_ID, 1, zeroPrice);
            assertThat(item.getUnitPrice().getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("line total")
    class LineTotal {
        @Test
        void lineTotalIsUnitPriceTimesQuantity() {
            OrderItem item = new OrderItem(PRODUCT_ID, 3, new Money(new BigDecimal("10.00")));
            Money total = item.getLineTotal();
            assertThat(total.getAmount()).isEqualByComparingTo("30.00");
        }
    }
}
