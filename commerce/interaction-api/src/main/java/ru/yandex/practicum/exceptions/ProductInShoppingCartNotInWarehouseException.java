package ru.yandex.practicum.exceptions;

public class ProductInShoppingCartNotInWarehouseException extends RuntimeException {
    public ProductInShoppingCartNotInWarehouseException(String message) {
        super(message);
    }
}
