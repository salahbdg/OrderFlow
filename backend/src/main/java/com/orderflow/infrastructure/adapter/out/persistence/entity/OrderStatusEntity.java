package com.orderflow.infrastructure.adapter.out.persistence.entity;

/**
 * Separate enum for JPA layer.
 * WHY NOT REUSE THE DOMAIN OrderStatus ENUM?
 *
 * If you use the domain enum directly in the JPA entity,
 * you've created a dependency from infrastructure → domain (ok)
 * AND from domain → infrastructure persistence concerns (not ok).
 *
 * More importantly: DB enums and domain enums can evolve separately.
 * A DB enum rename requires a migration. A domain rename requires
 * only a mapper update. Keeping them separate gives you that flexibility.
 *
 * For a simpler project you could reuse the domain enum — just know the trade-off.
 */
public enum OrderStatusEntity {
    PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED
}