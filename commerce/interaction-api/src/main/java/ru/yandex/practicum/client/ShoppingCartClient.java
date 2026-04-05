package ru.yandex.practicum.client;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.ShoppingCartDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "shopping-cart", path = "/api/v1/shopping-cart")
public interface ShoppingCartClient {
    @GetMapping
    ShoppingCartDto getActiveCart(@RequestParam("username") String username);

    @PutMapping
    ShoppingCartDto addToCart(@RequestParam("username") String username, @RequestBody Map<UUID, Integer> items);

    @DeleteMapping
    Boolean deactivateCart(@RequestParam("username") String username);

    @PostMapping("/remove")
    ShoppingCartDto removeProductsFromCart(@RequestParam("username") String username, @RequestBody List<UUID> productIds);

    @PostMapping("/change-quantity")
    ShoppingCartDto changeQuantity(
            @RequestParam("username") String username,
            @Valid @RequestBody ChangeProductQuantityRequest changeQuantity
    );
}
