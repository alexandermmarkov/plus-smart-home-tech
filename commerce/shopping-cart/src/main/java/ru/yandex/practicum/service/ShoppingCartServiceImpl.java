package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.client.WarehouseClient;
import ru.yandex.practicum.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.exceptions.CartNotFoundException;
import ru.yandex.practicum.exceptions.EmptyUsernameExeption;
import ru.yandex.practicum.exceptions.NoProductsInShoppingCartException;
import ru.yandex.practicum.exceptions.ProductInShoppingCartLowQuantityInWarehouseException;
import ru.yandex.practicum.exceptions.ProductNotFoundException;
import ru.yandex.practicum.model.CartItem;
import ru.yandex.practicum.model.ShoppingCart;
import ru.yandex.practicum.model.ShoppingCartMapper;
import ru.yandex.practicum.repository.ShoppingCartRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {
    private final ShoppingCartRepository cartRepository;
    private final ShoppingCartMapper cartMapper;
    private final WarehouseClient warehouseClient;

    @Override
    @Transactional
    public ShoppingCartDto addProducts(String username, Map<UUID, Integer> products) {
        usernameNotEmptyOrThrow(username);

        if (products == null || products.isEmpty()) {
            throw new NoProductsInShoppingCartException("Список товаров для добавления в корзину не должен быть пустым");
        }

        ShoppingCart cart = cartRepository.findByUsernameAndActiveTrue(username)
                .orElseGet(() -> createNewActiveCart(username));

        setProductsToCart(cart, products);

        try {
            warehouseClient.checkAvailabilityForCart(cartMapper.toDto(cart));
        } catch (ProductInShoppingCartLowQuantityInWarehouseException e) {
            // Пробрасываем бизнес-исключение как есть
            throw e;
        } catch (Exception ex) {
            throw new RuntimeException(
                    "Невозможно проверить наличие товаров: " + ex.getMessage());
        }

        cart = cartRepository.save(cart);
        return cartMapper.toDto(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public ShoppingCartDto getActiveShoppingCart(String username) {
        usernameNotEmptyOrThrow(username);
        ShoppingCart cart = getActiveCartOrThrow(username);
        return cartMapper.toDto(cart);
    }

    @Override
    @Transactional
    public Boolean deactivateShoppingCart(String username) {
        usernameNotEmptyOrThrow(username);
        ShoppingCart cart = getActiveCartOrThrow(username);
        cart.setActive(false);
        cartRepository.save(cart);
        return true;
    }

    @Override
    @Transactional
    public ShoppingCartDto removeProducts(String username, List<UUID> productIds) {
        usernameNotEmptyOrThrow(username);
        ShoppingCart cart = getActiveCartOrThrow(username);
        if (productIds == null || productIds.isEmpty()) {
            throw new IllegalArgumentException("Список продуктов к удалению из корзины не должен быть пустым");
        }

        Set<UUID> cartProductIds = cart.getItems().stream()
                .map(CartItem::getProductId)
                .collect(Collectors.toSet());

        // Пересечение
        boolean anyFound = productIds.stream().anyMatch(cartProductIds::contains);
        if (!anyFound) {
            throw new ProductNotFoundException("Нет искомых товаров " + productIds + " в корзине " + username);
        }

        cart.getItems().removeIf(item -> productIds.contains(item.getProductId()));

        cart = cartRepository.save(cart);
        return cartMapper.toDto(cart);
    }

    @Override
    @Transactional
    public ShoppingCartDto changeQuantity(String username, ChangeProductQuantityRequest changeQuantity) {
        usernameNotEmptyOrThrow(username);
        ShoppingCart cart = getActiveCartOrThrow(username);

        if (changeQuantity == null) {
            throw new IllegalArgumentException("changeQuantity == null");
        }
        UUID productId = changeQuantity.getProductId();
        Integer newQuantity = changeQuantity.getNewQuantity();

        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();
        if (existingItemOpt.isEmpty()) {
            throw new ProductNotFoundException("Товар " + productId + " не найден в корзине " + username);
        }

        existingItemOpt.get().setQuantity(newQuantity);

        warehouseClient.checkAvailabilityForCart(cartMapper.toDto(cart));

        cart = cartRepository.save(cart);
        return cartMapper.toDto(cart);
    }

    private ShoppingCart createNewActiveCart(String username) {
        return ShoppingCart.builder()
                .username(username)
                .active(true)
                .build();
    }

    private void setProductsToCart(ShoppingCart cart, Map<UUID, Integer> products) {
        if (products == null || products.isEmpty()) {
            return;
        }
        log.info("cart.getItems() = {}", cart.getItems());

        Map<UUID, CartItem> existingItems = cart.getItems().stream()
                .collect(Collectors.toMap(CartItem::getProductId, Function.identity()));

        for (Map.Entry<UUID, Integer> entry : products.entrySet()) {
            UUID productId = entry.getKey();
            Integer quantity = entry.getValue();
            if (quantity == null || quantity <= 0) {
                throw new IllegalArgumentException("Количество продукта " + productId + " = " + quantity);
            }
            CartItem existingItem = existingItems.get(productId);
            if (existingItem != null) {
                existingItem.setQuantity(quantity);
            } else {
                CartItem newItem = new CartItem();
                newItem.setProductId(productId);
                newItem.setQuantity(quantity);
                newItem.setShoppingCart(cart);
                cart.getItems().add(newItem);
            }
        }
    }

    private void usernameNotEmptyOrThrow(String username) {
        if (username == null || username.isBlank()) {
            throw new EmptyUsernameExeption("Имя пользователя не должно быть пустым");
        }
    }

    private ShoppingCart getActiveCartOrThrow(String username) {
        return cartRepository.findByUsernameAndActiveTrue(username).orElseThrow(() ->
                new CartNotFoundException("Не найдена активная корзина пользователя " + username));
    }

}
