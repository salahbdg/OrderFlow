package com.orderflow.infrastructure.adapter.out.persistence.mapper;

import com.orderflow.domain.model.Money;
import com.orderflow.domain.model.Product;
import com.orderflow.infrastructure.adapter.out.persistence.entity.ProductEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ProductMapper {

    public Product toDomain(ProductEntity entity) {
        return new Product(
                entity.getId(),
                entity.getName(),
                Money.of(entity.getPriceAmount(), entity.getPriceCurrency()),
                entity.getStockQuantity()
        );
    }

    public ProductEntity toEntity(Product product) {
        ProductEntity entity = new ProductEntity();
        entity.setId(product.getId());
        entity.setName(product.getName());
        entity.setPriceAmount(product.getPrice().getAmount());
        entity.setPriceCurrency(product.getPrice().getCurrencyCode());
        entity.setStockQuantity(product.getStockQuantity());
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        return entity;
    }
}