package com.orderflow.domain.port.out;

import com.orderflow.domain.model.Order;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Notice this interface uses domain objects (Order), NOT JPA entities.
 * The domain defines what it needs in its own language.
 * The infrastructure translates between domain objects and database rows.
 *
 * This means you could swap PostgreSQL for MongoDB tomorrow
 * by writing a new adapter — without touching a single domain class.
 */
public interface OrderRepositoryPort {
    Order save(Order order);
    Optional<Order> findById(UUID orderId);
    List<Order> findByCustomerId(UUID customerId);
    boolean existsById(UUID orderId);
}