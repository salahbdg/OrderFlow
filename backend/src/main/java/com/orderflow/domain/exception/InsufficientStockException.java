package com.orderflow.domain.exception;

import java.util.UUID;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(UUID productId, int requested) {
        super("Insufficient stock for product " + productId +
              ". Requested: " + requested);
    }
}