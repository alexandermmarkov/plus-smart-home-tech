package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.ShoppingCartDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ShoppingCartService {
    ShoppingCartDto addProducts(String username, Map<UUID, Integer> products);

    ShoppingCartDto getActiveShoppingCart(String username);

    Boolean deactivateShoppingCart(String username);

    ShoppingCartDto removeProducts(String username, List<UUID> productIds);

    ShoppingCartDto changeQuantity(String username, ChangeProductQuantityRequest changeQuantity);

}
