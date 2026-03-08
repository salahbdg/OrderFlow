package com.orderflow.application.service;

import com.orderflow.domain.event.OrderCreatedEvent;
import com.orderflow.domain.exception.InsufficientStockException;
import com.orderflow.domain.model.Money;
import com.orderflow.domain.model.Order;
import com.orderflow.domain.model.OrderStatus;
import com.orderflow.domain.model.Product;
import com.orderflow.domain.port.in.CreateOrderUseCase;
import com.orderflow.domain.port.out.OrderEventPublisherPort;
import com.orderflow.domain.port.out.OrderRepositoryPort;
import com.orderflow.domain.port.out.ProductRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test for OrderService.
 *
 * KEY PRINCIPLE: This test has ZERO infrastructure dependencies.
 * No Spring context, no database, no Kafka broker.
 * It runs in ~200ms. A Spring integration test runs in ~8 seconds.
 * Multiply that by 500 tests in a real codebase.
 *
 * @Mock creates fake implementations of the port interfaces.
 * We control exactly what they return, so we test ONLY
 * the OrderService logic in complete isolation.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepositoryPort orderRepository;
    @Mock private ProductRepositoryPort productRepository;
    @Mock private OrderEventPublisherPort eventPublisher;

    private OrderService orderService;

    private UUID customerId;
    private UUID productId;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        // Manual construction — no Spring needed
        orderService = new OrderService(
                orderRepository, productRepository, eventPublisher
        );

        customerId  = UUID.randomUUID();
        productId   = UUID.randomUUID();
        testProduct = new Product(
                productId, "Laptop",
                Money.of(new BigDecimal("999.99"), "EUR"),
                10  // 10 in stock
        );
    }

    @Test
    @DisplayName("Should create order successfully when product is in stock")
    void shouldCreateOrderSuccessfully() {
        // ARRANGE
        var command = new CreateOrderUseCase.CreateOrderCommand(
                customerId,
                List.of(new CreateOrderUseCase.OrderItemData(productId, 2))
        );

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(testProduct));

        // Make save() return whatever order it receives
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // ACT
        Order result = orderService.createOrder(command);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.getCustomerId()).isEqualTo(customerId);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.calculateTotal())
                .isEqualTo(Money.of(new BigDecimal("1999.98"), "EUR"));
    }

    @Test
    @DisplayName("Should publish OrderCreatedEvent after successful order creation")
    void shouldPublishOrderCreatedEvent() {
        // ARRANGE
        var command = new CreateOrderUseCase.CreateOrderCommand(
                customerId,
                List.of(new CreateOrderUseCase.OrderItemData(productId, 1))
        );

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(testProduct));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // ACT
        orderService.createOrder(command);

        // ASSERT — verify the event was published with correct data
        ArgumentCaptor<OrderCreatedEvent> eventCaptor =
                ArgumentCaptor.forClass(OrderCreatedEvent.class);

        verify(eventPublisher).publishOrderCreated(eventCaptor.capture());

        OrderCreatedEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.customerId()).isEqualTo(customerId);
        assertThat(publishedEvent.totalAmount())
                .isEqualTo(Money.of(new BigDecimal("999.99"), "EUR"));
    }

    @Test
    @DisplayName("Should throw exception when product is out of stock")
    void shouldThrowWhenProductOutOfStock() {
        // ARRANGE — only 1 in stock, requesting 5
        var outOfStockProduct = new Product(
                productId, "Laptop",
                Money.of(new BigDecimal("999.99"), "EUR"),
                1
        );

        var command = new CreateOrderUseCase.CreateOrderCommand(
                customerId,
                List.of(new CreateOrderUseCase.OrderItemData(productId, 5))
        );

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(outOfStockProduct));

        // ACT & ASSERT
        assertThatThrownBy(() -> orderService.createOrder(command))
                .isInstanceOf(InsufficientStockException.class);

        // Verify nothing was saved and no event was published
        verifyNoInteractions(orderRepository);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    @DisplayName("Should confirm order and publish OrderConfirmedEvent")
    void shouldConfirmOrderAndPublishEvent() {
        // ARRANGE — create a PENDING order to confirm
        var pendingOrder = Order.create(
                customerId,
                List.of(new com.orderflow.domain.model.OrderItem(
                        productId, "Laptop",
                        2,
                        Money.of(new BigDecimal("999.99"), "EUR")
                ))
        );
        // Drain the creation events so they don't interfere
        pendingOrder.pullDomainEvents();

        when(orderRepository.findById(pendingOrder.getId()))
                .thenReturn(Optional.of(pendingOrder));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // ACT
        Order result = orderService.updateStatus(
                pendingOrder.getId(), OrderStatus.CONFIRMED
        );

        // ASSERT
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        verify(eventPublisher)
                .publishOrderConfirmed(any());
    }
}