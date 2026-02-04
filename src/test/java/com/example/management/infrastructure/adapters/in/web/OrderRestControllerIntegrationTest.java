package com.example.management.infrastructure.adapters.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Order REST API")
class OrderRestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("POST /orders")
    class CreateOrder {

        @Test
        void creates_order_and_returns_201_with_body() throws Exception {
            UUID customerId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();
            String body = objectMapper.writeValueAsString(new CreateOrderRequestDto(
                    customerId,
                    List.of(new OrderItemDto(productId, 2, new BigDecimal("15.00"), "USD"))
            ));

            mockMvc.perform(post("/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                    .andExpect(jsonPath("$.status").value("PENDING"))
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.items.length()").value(1))
                    .andExpect(jsonPath("$.totalAmount").value(30))
                    .andExpect(jsonPath("$.totalCurrency").value("USD"));
        }

        @Test
        void returns_400_when_no_items() throws Exception {
            String body = objectMapper.writeValueAsString(new CreateOrderRequestDto(
                    UUID.randomUUID(),
                    List.of()
            ));
            mockMvc.perform(post("/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /orders/{id}")
    class GetOrder {

        @Test
        void returns_200_and_order_when_found() throws Exception {
            UUID customerId = UUID.randomUUID();
            String createBody = objectMapper.writeValueAsString(new CreateOrderRequestDto(
                    customerId,
                    List.of(new OrderItemDto(UUID.randomUUID(), 1, new BigDecimal("10.00"), "USD"))
            ));
            String created = mockMvc.perform(post("/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createBody))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            String id = objectMapper.readTree(created).get("id").asText();

            mockMvc.perform(get("/orders/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id))
                    .andExpect(jsonPath("$.customerId").value(customerId.toString()));
        }

        @Test
        void returns_404_when_not_found() throws Exception {
            mockMvc.perform(get("/orders/{id}", UUID.randomUUID()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /orders/{id}/mark-paid")
    class MarkAsPaid {

        @Test
        void returns_204_when_success() throws Exception {
            String createBody = objectMapper.writeValueAsString(new CreateOrderRequestDto(
                    UUID.randomUUID(),
                    List.of(new OrderItemDto(UUID.randomUUID(), 2, new BigDecimal("10.00"), "USD"))
            ));
            String created = mockMvc.perform(post("/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createBody))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            String id = objectMapper.readTree(created).get("id").asText();

            mockMvc.perform(post("/orders/{id}/mark-paid", id))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/orders/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("PAID"));
        }

        @Test
        void returns_400_when_total_below_minimum() throws Exception {
            String createBody = objectMapper.writeValueAsString(new CreateOrderRequestDto(
                    UUID.randomUUID(),
                    List.of(new OrderItemDto(UUID.randomUUID(), 1, new BigDecimal("5.00"), "USD"))
            ));
            String created = mockMvc.perform(post("/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createBody))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            String id = objectMapper.readTree(created).get("id").asText();

            mockMvc.perform(post("/orders/{id}/mark-paid", id))
                    .andExpect(status().isBadRequest());
        }
    }

    private record CreateOrderRequestDto(java.util.UUID customerId, List<OrderItemDto> items) {}
    private record OrderItemDto(java.util.UUID productId, int quantity, BigDecimal unitPriceAmount, String unitPriceCurrency) {}
}
