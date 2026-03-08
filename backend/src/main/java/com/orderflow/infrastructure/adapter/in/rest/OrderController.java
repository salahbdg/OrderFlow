package com.orderflow.infrastructure.adapter.in.rest;

import com.orderflow.domain.port.in.CreateOrderUseCase;
import com.orderflow.domain.port.in.CreateOrderUseCase.CreateOrderCommand;
import com.orderflow.domain.port.in.CreateOrderUseCase.OrderItemData;
import com.orderflow.domain.port.in.GetOrderUseCase;
import com.orderflow.infrastructure.adapter.in.rest.dto.CreateOrderRequest;
import com.orderflow.infrastructure.adapter.in.rest.dto.OrderResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST CONTROLLER — HTTP adapter.
 *
 * Responsibilities:
 * 1. Parse HTTP request → build command object
 * 2. Call the use case (via port interface — NOT OrderService directly)
 * 3. Map domain result → HTTP response DTO
 * 4. Return correct HTTP status code
 *
 * What it must NOT do:
 * - Contain business logic
 * - Talk to a repository directly
 * - Know anything about Kafka
 *
 * Notice it depends on USE CASE INTERFACES, not on OrderService.
 * This is the Dependency Inversion Principle in practice.
 */
@RestController
@RequestMapping("/api/v1/orders")
@CrossOrigin(origins = "http://localhost:4200")  // Angular dev server
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;

    public OrderController(CreateOrderUseCase createOrderUseCase,
                           GetOrderUseCase getOrderUseCase) {
        this.createOrderUseCase = createOrderUseCase;
        this.getOrderUseCase = getOrderUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        // Map DTO → command
        var command = new CreateOrderCommand(
                request.customerId(),
                request.items().stream()
                        .map(item -> new OrderItemData(item.productId(), item.quantity()))
                        .toList()
        );

        // Execute use case
        var order = createOrderUseCase.createOrder(command);

        // Map domain → response DTO
        return OrderResponse.fromDomain(order);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID orderId) {
        var order = getOrderUseCase.getOrderById(orderId);
        return ResponseEntity.ok(OrderResponse.fromDomain(order));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByCustomer(
            @PathVariable UUID customerId) {
        var orders = getOrderUseCase.getOrdersByCustomer(customerId)
                .stream()
                .map(OrderResponse::fromDomain)
                .toList();
        return ResponseEntity.ok(orders);
    }
}