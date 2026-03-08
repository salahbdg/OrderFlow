package com.orderflow.domain.port.out;

import com.orderflow.domain.model.Product;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepositoryPort {
    Optional<Product> findById(UUID productId);
    List<Product> findAll();
    Product save(Product product);
}