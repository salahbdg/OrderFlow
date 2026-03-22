package com.orderflow.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA ENTITY — represents a row in the 'orders' table.
 *
 * Notice what's NOT here:
 * - No business methods (no confirm(), no ship())
 * - No domain event collection
 * - No state transition validation
 *
 * This is a pure data container for ORM mapping.
 * All intelligence lives in the domain Order class.
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class OrderEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "customer_id", nullable = false, columnDefinition = "uuid")
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatusEntity status;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "total_currency", nullable = false, length = 3)
    private String totalCurrency;

    @OneToMany(
        mappedBy = "order",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.EAGER    // Small list, safe to eager load
    )
    private List<OrderItemEntity> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}