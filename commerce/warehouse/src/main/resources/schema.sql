CREATE SCHEMA IF NOT EXISTS warehouse;

CREATE TABLE IF NOT EXISTS warehouse.warehouse_products
(
    product_id UUID PRIMARY KEY,
    quantity   INTEGER NOT NULL DEFAULT 0,
    weight     NUMERIC(10,3) NOT NULL,
    width      NUMERIC(10,3) NOT NULL,
    height     NUMERIC(10,3) NOT NULL,
    depth      NUMERIC(10,3) NOT NULL,
    fragile    BOOLEAN NOT NULL DEFAULT FALSE
);