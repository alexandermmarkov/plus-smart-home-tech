CREATE SCHEMA IF NOT EXISTS shopping_store;

CREATE TABLE IF NOT EXISTS shopping_store.products
(
    product_id UUID PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    image_src VARCHAR(1024),
    quantity_state VARCHAR(20) NOT NULL,
    product_state VARCHAR(20) NOT NULL,
    product_category VARCHAR(20) NOT NULL,
    price NUMERIC(12, 2) NOT NULL
)