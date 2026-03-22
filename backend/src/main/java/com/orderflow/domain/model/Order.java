package com.orderflow.domain.model;

import com.orderflow.domain.event.OrderConfirmedEvent;
import com.orderflow.domain.event.OrderCreatedEvent;
import com.orderflow.domain.exception.InvalidOrderStateException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * ORDER — The Aggregate Root of the Order bounded context.
 *
 * This class is responsible for:
 * 1. Enforcing ALL business rules related to orders
 * 2. Maintaining its own consistency (you cannot create an invalid Order)
 * 3. Recording domain events (things that happened)
 *
 * WHAT IT IS NOT RESPONSIBLE FOR:
 * - Saving itself to a database (that's the repository's job)
 * - Sending Kafka messages (that's the event publisher's job)
 * - Validating HTTP requests (that's the controller's job)
 *
 * If you find yourself injecting a Spring @Service into this class,
 * stop — you've broken the architecture.
 */
public class Order {

    private final UUID id;
    private final UUID customerId;
    private final List<OrderItem> items;
    private OrderStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    /**
     * Domain events recorded during this session.
     * These are NOT persisted in the order table.
     * They are collected here and published AFTER the order is saved.
     *
     * This pattern is called "Domain Event Collection" and solves the
     * classic problem: how do you publish a Kafka event atomically
     * with saving to the database?
     * Answer: save first, then publish the collected events.
     */
    private final List<Object> domainEvents = new ArrayList<>();

    // Private constructor — use factory methods below
    private Order(UUID id, UUID customerId, List<OrderItem> items,
                  OrderStatus status, Instant createdAt, Instant updatedAt) {
        this.id         = id;
        this.customerId = customerId;
        this.items      = new ArrayList<>(items);
        this.status     = status;
        this.createdAt  = createdAt;
        this.updatedAt  = updatedAt;
    }

    /**
     * FACTORY METHOD: Create a brand new order.
     *
     * WHY A FACTORY METHOD INSTEAD OF A PUBLIC CONSTRUCTOR?
     * A constructor named Order() tells you nothing about intent.
     * Order.create(...) tells you exactly what's happening.
     * Factory methods can also enforce business rules before the
     * object even exists — if the rules fail, the object is never created.
     */
    public static Order create(UUID customerId, List<OrderItem> items) {
        // Business rule: an order must have at least one item
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        // Business rule: an order cannot have more than 50 items
        if (items.size() > 50) {
            throw new IllegalArgumentException("Order cannot exceed 50 items");
        }

        UUID orderId = UUID.randomUUID();
        Instant now = Instant.now();

        Order order = new Order(orderId, customerId, items,
                               OrderStatus.PENDING, now, now);

        // Record that this event happened — it will be published later
        order.domainEvents.add(new OrderCreatedEvent(orderId, customerId,
                                                     order.calculateTotal()));

        return order;
    }

    /**
     * RECONSTITUTION METHOD: Rebuild an order from persisted data.
     *
     * This is used by the repository when loading from the database.
     * It does NOT record domain events — the order already exists,
     * we're just rebuilding it in memory.
     */
    public static Order reconstitute(UUID id, UUID customerId, List<OrderItem> items,
                                     OrderStatus status, Instant createdAt, Instant updatedAt) {
        return new Order(id, customerId, items, status, createdAt, updatedAt);
    }

    /**
     * Business operation: confirm the order.
     * Called when both payment AND inventory are confirmed.
     */
    public void confirm() {
        transitionTo(OrderStatus.CONFIRMED);
        this.updatedAt = Instant.now();
        domainEvents.add(new OrderConfirmedEvent(this.id, this.customerId,
                                                  calculateTotal()));
    }

    public void startProcessing() {
        transitionTo(OrderStatus.PROCESSING);
        this.updatedAt = Instant.now();
    }

    public void ship() {
        transitionTo(OrderStatus.SHIPPED);
        this.updatedAt = Instant.now();
    }

    public void deliver() {
        transitionTo(OrderStatus.DELIVERED);
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        transitionTo(OrderStatus.CANCELLED);
        this.updatedAt = Instant.now();
    }

    /**
     * Derived value: total price of all items.
     * Calculated from items, never stored separately.
     * This ensures the total is ALWAYS consistent with the items.
     */
    public Money calculateTotal() {
        return items.stream()
                .map(OrderItem::totalPrice)
                .reduce(Money.zero("EUR"), Money::add);
    }

    /**
     * Enforces valid state transitions using OrderStatus rules.
     * This single method is the gatekeeper for all status changes.
     */
    private void transitionTo(OrderStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new InvalidOrderStateException(
                "Cannot transition order " + id +
                " from " + this.status + " to " + newStatus
            );
        }
        this.status = newStatus;
    }

    // Domain events are consumed once, then cleared
    public List<Object> pullDomainEvents() {
        List<Object> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return events;
    }

    // Getters — no setters. State changes only through business methods.
    public UUID getId()           { return id; }
    public UUID getCustomerId()   { return customerId; }
    public OrderStatus getStatus(){ return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    // Return unmodifiable view — nobody outside can mutate our items list
    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}