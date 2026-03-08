package com.orderflow.application.service;

import com.orderflow.domain.exception.InsufficientStockException;
import com.orderflow.domain.exception.OrderNotFoundException;
import com.orderflow.domain.exception.ProductNotFoundException;
import com.orderflow.domain.model.Order;
import com.orderflow.domain.model.OrderItem;
import com.orderflow.domain.model.OrderStatus;
import com.orderflow.domain.port.in.CreateOrderUseCase;
import com.orderflow.domain.port.in.GetOrderUseCase;
import com.orderflow.domain.port.in.UpdateOrderStatusUseCase;
import com.orderflow.domain.port.out.OrderEventPublisherPort;
import com.orderflow.domain.port.out.OrderRepositoryPort;
import com.orderflow.domain.port.out.ProductRepositoryPort;

import java.util.List;
import java.util.UUID;

/**
 * APPLICATION SERVICE — Orchestrates the order lifecycle.
 *
 * WHY NO @Service ANNOTATION HERE?
 * Notice there's no Spring annotation on this class.
 * The wiring to Spring happens in infrastructure/config/BeanConfig.java.
 * This keeps the application layer free of framework dependencies.
 *
 * Some teams do put @Service here for convenience — that's a trade-off.
 * The purist approach (what we're doing) makes this class
 * unit-testable with zero Spring context. A test runs in milliseconds,
 * not seconds. At scale, this matters enormously.
 *
 * This class implements THREE use case interfaces.
 * Each interface is a separate contract — controllers depend on
 * the interface, not on this concrete class.
 */
public class OrderService implements
        CreateOrderUseCase,
        GetOrderUseCase,
        UpdateOrderStatusUseCase {

    private final OrderRepositoryPort orderRepository;
    private final ProductRepositoryPort productRepository;
    private final OrderEventPublisherPort eventPublisher;

    // Constructor injection — no @Autowired needed, and it makes
    // dependencies explicit. You CANNOT create this object without
    // providing its dependencies. That's the point.
    public OrderService(
            OrderRepositoryPort orderRepository,
            ProductRepositoryPort productRepository,
            OrderEventPublisherPort eventPublisher
    ) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * USE CASE: Create a new order.
     *
     * THE FLOW:
     * 1. Validate each product exists and is in stock
     * 2. Build OrderItem list with CURRENT prices (snapshot)
     * 3. Tell the Order domain object to create itself
     * 4. Persist the order
     * 5. Publish domain events collected during creation
     * 6. Return the created order
     *
     * Notice: this method coordinates but makes no business decisions.
     * Business decisions live in Order.create() and OrderItem constructor.
     */
    @Override
    public Order createOrder(CreateOrderCommand command) {

        // Step 1 & 2: Resolve each product and build order items
        List<OrderItem> orderItems = command.items().stream()
                .map(itemData -> {
                    // Load product from repository (via port — not JPA directly)
                    var product = productRepository.findById(itemData.productId())
                            .orElseThrow(() -> new ProductNotFoundException(
                                    itemData.productId()
                            ));

                    // Validate stock
                    if (!product.hasStock(itemData.quantity())) {
                        throw new InsufficientStockException(
                                product.getId(), itemData.quantity()
                        );
                    }

                    // Build the item with a PRICE SNAPSHOT
                    return new OrderItem(
                            product.getId(),
                            product.getName(),
                            itemData.quantity(),
                            product.getPrice()   // snapshot at this moment
                    );
                })
                .toList();

        // Step 3: Domain creates the order and records events internally
        Order order = Order.create(command.customerId(), orderItems);

        // Step 4: Persist — save BEFORE publishing events
        // If the save fails, no event gets published. Correct behavior.
        Order savedOrder = orderRepository.save(order);

        // Step 5: Pull and publish events collected by the domain object
        // If event publishing fails, the order is already saved.
        // This is an intentional trade-off: at-least-once delivery.
        // We'll handle idempotency on the consumer side.
        publishDomainEvents(savedOrder);

        return savedOrder;
    }

    @Override
    public Order getOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    @Override
    public List<Order> getOrdersByCustomer(UUID customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    /**
     * USE CASE: Update order status.
     *
     * This is called by Kafka consumers when downstream events arrive.
     * Example: PaymentProcessedEvent + InventoryReservedEvent
     * both arrive → status transitions to CONFIRMED.
     *
     * The domain's canTransitionTo() enforces that only
     * legal transitions are allowed.
     */
    @Override
    public Order updateStatus(UUID orderId, OrderStatus newStatus) {
        // Load from DB → get the live domain object
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // Delegate the state change to the domain object
        // The domain enforces transition rules — NOT this service
        switch (newStatus) {
            case CONFIRMED   -> order.confirm();
            case PROCESSING  -> order.startProcessing();
            case SHIPPED     -> order.ship();
            case DELIVERED   -> order.deliver();
            case CANCELLED   -> order.cancel();
            default -> throw new IllegalArgumentException(
                    "Cannot manually set status to: " + newStatus
            );
        }

        // Persist the updated order
        Order updatedOrder = orderRepository.save(order);

        // Publish any events the domain recorded (e.g. OrderConfirmedEvent)
        publishDomainEvents(updatedOrder);

        return updatedOrder;
    }

    /**
     * Pulls domain events from the aggregate and routes them
     * to the correct publisher method.
     *
     * WHY CHECK instanceof?
     * The domain collects events as List<Object> because it doesn't
     * know about specific event types at compile time (open/closed principle).
     * We pattern match here at the application boundary.
     *
     * Java 21's pattern matching makes this clean.
     */
    private void publishDomainEvents(Order order) {
        order.pullDomainEvents().forEach(event -> {
            switch (event) {
                case com.orderflow.domain.event.OrderCreatedEvent e
                        -> eventPublisher.publishOrderCreated(e);
                case com.orderflow.domain.event.OrderConfirmedEvent e
                        -> eventPublisher.publishOrderConfirmed(e);
                default -> throw new IllegalStateException(
                        "Unknown domain event: " + event.getClass()
                );
            }
        });
    }
}