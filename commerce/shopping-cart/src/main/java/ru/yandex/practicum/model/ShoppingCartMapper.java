package ru.yandex.practicum.model;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.ShoppingCartDto;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ShoppingCartMapper {

    public ShoppingCartDto toDto(ShoppingCart cart) {
        Map<UUID, Integer> products = cart.getItems().stream()
                .collect(Collectors.toMap(
                        CartItem::getProductId,
                        CartItem::getQuantity
                ));
        return new ShoppingCartDto(cart.getShoppingCartId(), products);
    }

}
