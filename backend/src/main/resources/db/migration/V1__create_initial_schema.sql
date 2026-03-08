-- V1__create_initial_schema.sql
-- Flyway naming convention: V{version}__{description}.sql
-- Version numbers must always increase. Never edit an existing migration.
-- If you need to change something, create a new migration file.

-- CUSTOMERS table
CREATE TABLE customers (
    id          UUID PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    email       VARCHAR(255) NOT NULL UNIQUE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- PRODUCTS table
CREATE TABLE products (
    id              UUID PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    price_amount    NUMERIC(19, 2) NOT NULL,     -- Never use FLOAT for money
    price_currency  VARCHAR(3) NOT NULL DEFAULT 'EUR',
    stock_quantity  INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT price_positive CHECK (price_amount >= 0),
    CONSTRAINT stock_non_negative CHECK (stock_quantity >= 0)
);

-- ORDERS table
CREATE TABLE orders (
    id              UUID PRIMARY KEY,
    customer_id     UUID NOT NULL REFERENCES customers(id),
    status          VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    total_amount    NUMERIC(19, 2) NOT NULL,
    total_currency  VARCHAR(3) NOT NULL DEFAULT 'EUR',
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT valid_status CHECK (status IN (
        'PENDING','CONFIRMED','PROCESSING','SHIPPED','DELIVERED','CANCELLED','REFUNDED'
    ))
);

-- ORDER_ITEMS table
-- Note: we store product_name and unit_price as a SNAPSHOT
-- This is intentional — see OrderItem.java comments
CREATE TABLE order_items (
    id              UUID PRIMARY KEY,
    order_id        UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id      UUID NOT NULL,               -- No FK — product may change
    product_name    VARCHAR(255) NOT NULL,        -- Snapshot
    quantity        INT NOT NULL,
    unit_price_amount   NUMERIC(19, 2) NOT NULL, -- Snapshot
    unit_price_currency VARCHAR(3) NOT NULL DEFAULT 'EUR',

    CONSTRAINT quantity_positive CHECK (quantity > 0),
    CONSTRAINT unit_price_positive CHECK (unit_price_amount >= 0)
);

-- INDEXES — always add indexes on columns you query by
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);

-- SEED DATA — for local development
INSERT INTO customers (id, name, email) VALUES
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Alice Martin', 'alice@example.com'),
    ('b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'Bob Smith',   'bob@example.com');

INSERT INTO products (id, name, description, price_amount, stock_quantity) VALUES
    ('c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'Laptop Pro',   'High-end laptop',   1299.99, 50),
    ('d3eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'Wireless Mouse','Ergonomic mouse',     49.99, 200),
    ('e4eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', 'USB-C Hub',    '7-port USB-C hub',    79.99, 100);