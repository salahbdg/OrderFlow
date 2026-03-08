package com.orderflow.domain.port.in;

import com.orderflow.domain.model.Order;
import java.util.List;
import java.util.UUID;

/**
 * A USE CASE interface is a single, focused business operation.
 *
 * WHY ONE METHOD PER INTERFACE?
 * This follows the Interface Segregation Principle.
 * The OrderController only needs to call CreateOrderUseCase.
 * It doesn't need to know that OrderService also handles getOrder.
 * This makes the system easier to test and change independently.
 *
 * In a test, you mock just CreateOrderUseCase — not a giant
 * OrderService with 20 methods.
 */
public interface CreateOrderUseCase {

    Order createOrder(CreateOrderCommand command);

    /**
     * Command objects carry the input data for a use case.
     * They replace method parameter lists longer than 3 arguments.
     * Using a record makes them immutable and self-documenting.
     */
    record CreateOrderCommand(
            UUID customerId,
            List<OrderItemData> items
    ) {}

    record OrderItemData(
            UUID productId,
            int quantity
    ) {}
}