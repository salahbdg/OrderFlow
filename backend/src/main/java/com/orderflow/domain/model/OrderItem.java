package com.orderflow.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a single line item within an order.
 *
 * WHY SNAPSHOT THE PRICE HERE?
 * This is a critical business decision. If a product's price changes
 * tomorrow, the customer's receipt from today must still show
 * what they ACTUALLY paid. We store a price snapshot at order time,
 * not a reference to the current product price.
 *
 * This is called "denormalization for business correctness" and is
 * extremely common in order management systems.
 */
public final class OrderItem {

    private final UUID productId;
    private final String productName;   // Snapshot — not a live reference
    private final int quantity;
    private final Money unitPrice;      // Snapshot — price at time of order

    public OrderItem(UUID productId, String productName, int quantity, Money unitPrice) {
        if (productId == null)    throw new IllegalArgumentException("Product ID required");
        if (productName == null || productName.isBlank())
                                  throw new IllegalArgumentException("Product name required");
        if (quantity <= 0)        throw new IllegalArgumentException("Quantity must be positive");
        if (unitPrice == null)    throw new IllegalArgumentException("Unit price required");

        this.productId   = productId;
        this.productName = productName;
        this.quantity    = quantity;
        this.unitPrice   = unitPrice;
    }

    // Derived value — calculated, not stored
    public Money totalPrice() {
        return unitPrice.multiply(quantity);
    }

    public UUID getProductId()     { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity()       { return quantity; }
    public Money getUnitPrice()    { return unitPrice; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderItem item)) return false;
        return quantity == item.quantity &&
               Objects.equals(productId, item.productId) &&
               Objects.equals(unitPrice, item.unitPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, quantity, unitPrice);
    }
}