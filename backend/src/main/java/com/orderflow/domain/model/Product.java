package com.orderflow.domain.model;

import java.util.UUID;

public class Product {

    private final UUID id;
    private final String name;
    private final Money price;
    private final int stockQuantity;

    public Product(UUID id, String name, Money price, int stockQuantity) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Product name required");
        if (price == null)
            throw new IllegalArgumentException("Product price required");
        if (stockQuantity < 0)
            throw new IllegalArgumentException("Stock cannot be negative");

        this.id            = id;
        this.name          = name;
        this.price         = price;
        this.stockQuantity = stockQuantity;
    }

    // Business rule lives ON the object that owns the data
    public boolean hasStock(int requestedQuantity) {
        return this.stockQuantity >= requestedQuantity;
    }

    public UUID getId()           { return id; }
    public String getName()       { return name; }
    public Money getPrice()       { return price; }
    public int getStockQuantity() { return stockQuantity; }
}