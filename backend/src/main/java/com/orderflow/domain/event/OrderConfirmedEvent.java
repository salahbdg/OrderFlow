package com.orderflow.domain.event;

import com.orderflow.domain.model.Money;
import java.time.Instant;
import java.util.UUID;

// Why record? Java records are perfect for domain events — they're immutable by default, 
// auto-generate equals/hashCode/toString, and read like documentation. A senior engineer 
// seeing a record immediately knows "this is an immutable data carrier."

public record OrderConfirmedEvent(
        UUID eventId,
        UUID orderId,
        UUID customerId,
        Money totalAmount,
        Instant occurredAt
) {
    public OrderConfirmedEvent(UUID orderId, UUID customerId, Money totalAmount) {
        this(UUID.randomUUID(), orderId, customerId, totalAmount, Instant.now());
    }
}