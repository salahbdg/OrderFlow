package com.orderflow.infrastructure.adapter.out.persistence.repository;

import com.orderflow.infrastructure.adapter.out.persistence.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, UUID> {}