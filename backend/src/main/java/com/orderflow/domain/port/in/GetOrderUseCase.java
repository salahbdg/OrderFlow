package com.orderflow.domain.port.in;

import com.orderflow.domain.model.Order;
import java.util.List;
import java.util.UUID;

public interface GetOrderUseCase {
    Order getOrderById(UUID orderId);
    List<Order> getOrdersByCustomer(UUID customerId);
}