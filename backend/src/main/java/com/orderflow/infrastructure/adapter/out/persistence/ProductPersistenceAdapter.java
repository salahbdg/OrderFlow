package com.orderflow.infrastructure.adapter.out.persistence;

import com.orderflow.domain.model.Product;
import com.orderflow.domain.port.out.ProductRepositoryPort;
import com.orderflow.infrastructure.adapter.out.persistence.mapper.ProductMapper;
import com.orderflow.infrastructure.adapter.out.persistence.repository.ProductJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ProductPersistenceAdapter implements ProductRepositoryPort {

    private final ProductJpaRepository jpaRepository;
    private final ProductMapper mapper;

    public ProductPersistenceAdapter(ProductJpaRepository jpaRepository,
                                     ProductMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Product> findById(UUID productId) {
        return jpaRepository.findById(productId).map(mapper::toDomain);
    }

    @Override
    public List<Product> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Product save(Product product) {
        var entity = mapper.toEntity(product);
        return mapper.toDomain(jpaRepository.save(entity));
    }
}