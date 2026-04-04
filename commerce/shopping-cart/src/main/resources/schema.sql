CREATE SCHEMA IF NOT EXISTS shopping_cart;

CREATE TABLE IF NOT EXISTS shopping_cart.carts (
    cart_id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS shopping_cart.cart_items (
    id UUID PRIMARY KEY,
    cart_id UUID NOT NULL REFERENCES shopping_cart.carts(cart_id) ON DELETE CASCADE,
    product_id UUID NOT NULL,
    quantity INTEGER NOT NULL,
    UNIQUE (cart_id, product_id)
);