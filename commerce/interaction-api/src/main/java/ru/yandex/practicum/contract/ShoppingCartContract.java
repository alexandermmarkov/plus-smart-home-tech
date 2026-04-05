package ru.yandex.practicum.contract;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.ShoppingCartDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ShoppingCartContract {
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
