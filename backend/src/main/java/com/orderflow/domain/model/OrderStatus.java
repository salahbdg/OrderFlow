package com.orderflow.domain.model;

/**
 * Represents every possible state an order can be in.
 *
 * WHY AN ENUM FOR STATUS?
 * Storing status as a plain String in the DB is a trap.
 * Nothing stops a bug from setting status to "SHIPED" (typo).
 * An enum makes invalid states impossible to represent.
 *
 * This enum also encodes TRANSITION RULES — which is a business rule,
 * and therefore belongs in the domain, not in a service or controller.
 */
public enum OrderStatus {
    PENDING,        // Order created, awaiting payment + inventory confirmation
    CONFIRMED,      // Payment processed AND inventory reserved
    PROCESSING,     // Being prepared for shipment
    SHIPPED,        // Handed to courier
    DELIVERED,      // Customer received the order
    CANCELLED,      // Cancelled before shipment
    REFUNDED;       // Payment returned to customer

    /**
     * Business rule: not every status transition is legal.
     * You cannot go from DELIVERED back to PENDING.
     * You cannot go from CANCELLED to SHIPPED.
     *
     * This enforces the rule at the domain level.
     */
    public boolean canTransitionTo(OrderStatus next) {
        return switch (this) {
            case PENDING    -> next == CONFIRMED || next == CANCELLED;
            case CONFIRMED  -> next == PROCESSING || next == CANCELLED;
            case PROCESSING -> next == SHIPPED || next == CANCELLED;
            case SHIPPED    -> next == DELIVERED;
            case DELIVERED  -> next == REFUNDED;
            case CANCELLED,
                 REFUNDED   -> false; // Terminal states
        };
    }
}