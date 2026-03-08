package com.orderflow.infrastructure.adapter.out.persistence.mapper;

import com.orderflow.domain.model.*;
import com.orderflow.infrastructure.adapter.out.persistence.entity.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * MAPPER — Translates between domain objects and JPA entities.
 *
 * This is the most tedious part of hexagonal architecture.
 * Teams often use MapStruct to generate this code automatically.
 * We write it manually here so you understand exactly what's happening.
 *
 * Rule: Mappers live in infrastructure. Domain objects never know
 * about JPA entities. JPA entities never contain business logic.
 */
@Component
public class OrderMapper {

    // ─── Domain → Entity (before saving to DB) ────────────────────────────

    public OrderEntity toEntity(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.setId(order.getId());
        entity.setCustomerId(order.getCustomerId());
        entity.setStatus(OrderStatusEntity.valueOf(order.getStatus().name()));
        entity.setTotalAmount(order.calculateTotal().getAmount());
        entity.setTotalCurrency(order.calculateTotal().getCurrencyCode());
        entity.setCreatedAt(order.getCreatedAt());
        entity.setUpdatedAt(order.getUpdatedAt());

        List<OrderItemEntity> itemEntities = order.getItems().stream()
                .map(item -> toItemEntity(item, entity))
                .toList();

        entity.getItems().clear();
        entity.getItems().addAll(itemEntities);

        return entity;
    }

    private OrderItemEntity toItemEntity(OrderItem item, OrderEntity orderEntity) {
        OrderItemEntity entity = new OrderItemEntity();
        entity.setId(UUID.randomUUID());
        entity.setOrder(orderEntity);
        entity.setProductId(item.getProductId());
        entity.setProductName(item.getProductName());
        entity.setQuantity(item.getQuantity());
        entity.setUnitPriceAmount(item.getUnitPrice().getAmount());
        entity.setUnitPriceCurrency(item.getUnitPrice().getCurrencyCode());
        return entity;
    }

    // ─── Entity → Domain (after loading from DB) ──────────────────────────

    public Order toDomain(OrderEntity entity) {
        List<OrderItem> items = entity.getItems().stream()
                .map(this::toItemDomain)
                .toList();

        return Order.reconstitute(
                entity.getId(),
                entity.getCustomerId(),
                items,
                OrderStatus.valueOf(entity.getStatus().name()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private OrderItem toItemDomain(OrderItemEntity entity) {
        return new OrderItem(
                entity.getProductId(),
                entity.getProductName(),
                entity.getQuantity(),
                Money.of(entity.getUnitPriceAmount(), entity.getUnitPriceCurrency())
        );
    }
}