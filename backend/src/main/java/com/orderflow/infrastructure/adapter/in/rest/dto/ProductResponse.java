package com.orderflow.infrastructure.adapter.in.rest.dto;

import com.orderflow.domain.model.Product;
import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        String currency,
        int stockQuantity
) {
    public static ProductResponse fromDomain(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                null,
                product.getPrice().getAmount(),
                product.getPrice().getCurrencyCode(),
                product.getStockQuantity()
        );
    }
}