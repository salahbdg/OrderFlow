package com.orderflow.infrastructure.adapter.out.persistence;

import com.orderflow.domain.model.Order;
import com.orderflow.domain.port.out.OrderRepositoryPort;
import com.orderflow.infrastructure.adapter.out.persistence.mapper.OrderMapper;
import com.orderflow.infrastructure.adapter.out.persistence.repository.OrderJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * PERSISTENCE ADAPTER — implements the domain port using JPA.
 *
 * This class is the bridge between the domain's language (Order objects)
 * and the database's language (OrderEntity rows).
 *
 * @Component registers it with Spring so BeanConfig can inject it
 * wherever OrderRepositoryPort is required.
 */
@Component
public class OrderPersistenceAdapter implements OrderRepositoryPort {

    private final OrderJpaRepository jpaRepository;
    private final OrderMapper mapper;

    public OrderPersistenceAdapter(OrderJpaRepository jpaRepository,
                                   OrderMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Order save(Order order) {
        var entity = mapper.toEntity(order);
        var savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Order> findById(UUID orderId) {
        return jpaRepository.findById(orderId)
                .map(mapper::toDomain);
    }

    @Override
    public List<Order> findByCustomerId(UUID customerId) {
        return jpaRepository.findByCustomerId(customerId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsById(UUID orderId) {
        return jpaRepository.existsById(orderId);
    }
}