package com.orderflow.infrastructure.adapter.in.rest.dto;

import com.orderflow.domain.model.Order;
import com.orderflow.domain.model.OrderItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * What we send BACK to Angular.
 *
 * Notice the static factory method fromDomain() —
 * the mapping logic lives on the DTO, keeping the controller clean.
 * Some teams put this in a separate mapper class.
 * Either is acceptable — consistency matters more than which you choose.
 */
public record OrderResponse(
        UUID id,
        UUID customerId,
        String status,
        BigDecimal totalAmount,
        String currency,
        List<OrderItemResponse> items,
        Instant createdAt,
        Instant updatedAt
) {
    public static OrderResponse fromDomain(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getStatus().name(),
                order.calculateTotal().getAmount(),
                order.calculateTotal().getCurrencyCode(),
                order.getItems().stream()
                        .map(OrderItemResponse::fromDomain)
                        .toList(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    public record OrderItemResponse(
            UUID productId,
            String productName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal totalPrice
    ) {
        public static OrderItemResponse fromDomain(OrderItem item) {
            return new OrderItemResponse(
                    item.getProductId(),
                    item.getProductName(),
                    item.getQuantity(),
                    item.getUnitPrice().getAmount(),
                    item.totalPrice().getAmount()
            );
        }
    }
}