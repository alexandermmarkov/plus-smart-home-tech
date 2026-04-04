package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.client.ShoppingCartClient;
import ru.yandex.practicum.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.service.ShoppingCartService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/shopping-cart")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ShoppingCartController implements ShoppingCartClient {
    private final ShoppingCartService cartService;

    @Override
    @GetMapping
    public ShoppingCartDto getActiveCart(@RequestParam String username) {
        log.info("Получить корзину username {}", username);
        ShoppingCartDto cartDto = cartService.getActiveShoppingCart(username);
        log.info("Возвращаем корзину username {}: {}", username, cartDto);
        return cartDto;
    }

    @Override
    @PutMapping
    public ShoppingCartDto addToCart(
            @RequestParam String username,
            @RequestBody Map<UUID, Integer> items) {
        log.info("Создать корзину username {}, {}", username, items);
        ShoppingCartDto cartDto = cartService.addProducts(username, items);
        log.info("Возвращаем новую корзину username {}: {}", username, cartDto);
        return cartDto;
    }

    @Override
    @DeleteMapping
    public Boolean deactivateCart(@RequestParam String username) {
        log.info("Деактивировать корзину username {}", username);
        Boolean result = cartService.deactivateShoppingCart(username);
        log.info("Результат деактивации корзину username {}: {}", username, result);
        return result;
    }

    @Override
    @PostMapping("/remove")
    public ShoppingCartDto removeProductsFromCart(
            @RequestParam String username,
            @RequestBody List<UUID> productIds
    ) {
        log.info("Удалить из корзины {} товары {}", username, productIds);
        ShoppingCartDto updatedCart = cartService.removeProducts(username, productIds);
        log.info("Корзина {} после удаления товаров: {}", username, updatedCart);
        return updatedCart;
    }

    @Override
    @PostMapping("/change-quantity")
    public ShoppingCartDto changeQuantity(
            @RequestParam String username,
            @Valid @RequestBody ChangeProductQuantityRequest changeQuantity
    ) {
        log.info("Изменить количество в корзине {}:  {}", username, changeQuantity);
        ShoppingCartDto updatedCart = cartService.changeQuantity(username, changeQuantity);
        log.info("Корзина {} после изменения количества: {}", username, updatedCart);
        return updatedCart;
    }

}
