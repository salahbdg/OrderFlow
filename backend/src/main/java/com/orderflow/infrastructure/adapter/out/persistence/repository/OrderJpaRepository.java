package com.orderflow.infrastructure.adapter.out.persistence.repository;

import com.orderflow.infrastructure.adapter.out.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository.
 * Lives in infrastructure — the domain never sees this interface.
 * The domain only sees OrderRepositoryPort.
 */
public interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID> {
    List<OrderEntity> findByCustomerId(UUID customerId);
}