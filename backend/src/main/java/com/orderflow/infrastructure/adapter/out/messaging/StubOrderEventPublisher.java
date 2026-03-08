package com.orderflow.infrastructure.adapter.out.messaging;

import com.orderflow.domain.event.OrderConfirmedEvent;
import com.orderflow.domain.event.OrderCreatedEvent;
import com.orderflow.domain.port.out.OrderEventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * STUB ADAPTER — temporary implementation used until Kafka is wired.
 *
 * This is a real pattern in production development.
 * You stub out external dependencies (email, Kafka, payment gateways)
 * so the rest of the system is testable independently.
 *
 * When Kafka is ready, we replace this with KafkaOrderEventPublisher
 * and change ONE line in BeanConfig. Everything else stays the same.
 */
@Component
public class StubOrderEventPublisher implements OrderEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(StubOrderEventPublisher.class);

    @Override
    public void publishOrderCreated(OrderCreatedEvent event) {
        log.info("[STUB] Would publish OrderCreatedEvent: orderId={}, total={}",
                event.orderId(), event.totalAmount());
    }

    @Override
    public void publishOrderConfirmed(OrderConfirmedEvent event) {
        log.info("[STUB] Would publish OrderConfirmedEvent: orderId={}",
                event.orderId());
    }
}