package com.orderflow.infrastructure.config;

import com.orderflow.application.service.OrderService;
import com.orderflow.domain.port.out.OrderEventPublisherPort;
import com.orderflow.domain.port.out.OrderRepositoryPort;
import com.orderflow.domain.port.out.ProductRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * THIS IS THE GLUE OF HEXAGONAL ARCHITECTURE.
 *
 * This single class is the only place in the entire codebase
 * where concrete implementations are wired to port interfaces.
 *
 * When a junior developer asks "where is OrderService instantiated?"
 * the answer is always: "look in BeanConfig."
 *
 * When you want to swap PostgreSQL for MongoDB, you:
 * 1. Write a new MongoOrderRepositoryAdapter
 * 2. Change ONE LINE in this file
 * 3. Done. Nothing else changes.
 *
 * That is the entire value proposition of hexagonal architecture.
 */
@Configuration
public class BeanConfig {

    @Bean
    public OrderService orderService(
            OrderRepositoryPort orderRepository,
            ProductRepositoryPort productRepository,
            OrderEventPublisherPort eventPublisher
    ) {
        return new OrderService(orderRepository, productRepository, eventPublisher);
    }
}

/*
Why does this work? Spring scans for @Bean methods. The parameters 
OrderRepositoryPort, ProductRepositoryPort, and OrderEventPublisherPort
 are interfaces. Spring will look for @Component or @Bean implementations 
 of those interfaces in the infrastructure layer and inject them 
 automatically. 
*/