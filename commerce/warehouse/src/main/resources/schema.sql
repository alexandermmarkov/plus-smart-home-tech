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
CREATE TABLE IF NOT EXISTS warehouse.bookings
(
    booking_id      UUID PRIMARY KEY,
    order_id        UUID,
    delivery_id     UUID,
    delivery_weight NUMERIC(10,3),
    delivery_volume NUMERIC(10,3),
    fragile    BOOLEAN
);
CREATE TABLE IF NOT EXISTS warehouse.booking_products (
    id          UUID PRIMARY KEY,
    booking_id  UUID NOT NULL,
    product_id  UUID NOT NULL,
    quantity    INTEGER NOT NULL,
    FOREIGN KEY (booking_id) REFERENCES warehouse.bookings(booking_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES warehouse.warehouse_products(product_id),
    UNIQUE (booking_id, product_id)
);
