package com.orderflow.infrastructure.adapter.in.rest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.UUID;

/**
 * DTO — Data Transfer Object.
 * This is what Angular sends us in the HTTP request body.
 * It is validated BEFORE reaching the application layer.
 */
public record CreateOrderRequest(

        @NotNull(message = "Customer ID is required")
        UUID customerId,

        @NotEmpty(message = "Order must contain at least one item")
        @Valid
        List<OrderItemRequest> items
) {
    public record OrderItemRequest(
            @NotNull(message = "Product ID is required")
            UUID productId,

            @Positive(message = "Quantity must be positive")
            int quantity
    ) {}
}