package com.orderflow.domain.port.in;

import com.orderflow.domain.model.Order;
import com.orderflow.domain.model.OrderStatus;
import java.util.UUID;

public interface UpdateOrderStatusUseCase {
    Order updateStatus(UUID orderId, OrderStatus newStatus);
}