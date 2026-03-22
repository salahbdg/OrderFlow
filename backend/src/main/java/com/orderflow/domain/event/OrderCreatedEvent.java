package com.orderflow.domain.event;

import com.orderflow.domain.model.Money;
import java.time.Instant;
import java.util.UUID;

/**
 * A Domain Event represents something that HAPPENED in the domain.
 * It is always named in the past tense: OrderCREATED, not CreateOrder.
 *
 * Domain events are immutable records of facts.
 * Once an order is created, that fact cannot be changed.
 */
public record OrderCreatedEvent(
        UUID eventId,
        UUID orderId,
        UUID customerId,
        Money totalAmount,
        Instant occurredAt
) {
    // Compact constructor with auto-generated eventId and timestamp
    public OrderCreatedEvent(UUID orderId, UUID customerId, Money totalAmount) {
        this(UUID.randomUUID(), orderId, customerId, totalAmount, Instant.now());
    }
}