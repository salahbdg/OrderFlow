package com.orderflow.domain.port.out;

import com.orderflow.domain.event.OrderConfirmedEvent;
import com.orderflow.domain.event.OrderCreatedEvent;

/**
 * The domain doesn't know Kafka exists.
 * It only knows it needs to "publish events somewhere."
 * The infrastructure wires this to Kafka.
 *
 * In tests, you can wire this to an in-memory list
 * and verify events were published without starting Kafka.
 */
public interface OrderEventPublisherPort {
    void publishOrderCreated(OrderCreatedEvent event);
    void publishOrderConfirmed(OrderConfirmedEvent event);
}