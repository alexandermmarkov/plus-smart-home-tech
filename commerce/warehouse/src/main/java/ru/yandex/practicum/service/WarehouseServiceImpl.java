package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.exceptions.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.exceptions.ProductInShoppingCartLowQuantityInWarehouseException;
import ru.yandex.practicum.exceptions.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.model.WarehouseProduct;
import ru.yandex.practicum.model.WarehouseProductMapper;
import ru.yandex.practicum.repository.WarehouseProductRepository;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseServiceImpl implements WarehouseService {
    private final WarehouseProductRepository warehouseRepository;
    private final WarehouseProductMapper warehouseMapper;

    private static final String[] ADDRESSES = new String[]{"ADDRESS_1", "ADDRESS_2"};
    private static final String CURRENT_ADDRESS =
            ADDRESSES[Random.from(new SecureRandom()).nextInt(0, ADDRESSES.length)];

    @Override
    @Transactional
    public void addNewProductToWarehouse(NewProductInWarehouseRequest newProduct) {
        if (warehouseRepository.existsById(newProduct.getProductId())) {
            throw new SpecifiedProductAlreadyInWarehouseException("Товар с таким описанием уже зарегистрирован на складе");
        }
        WarehouseProduct product = warehouseMapper.toModel(newProduct);
        warehouseRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public BookedProductsDto checkAvailabilityForCart(ShoppingCartDto shoppingCartDto) {
        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal totalVolume = BigDecimal.ZERO;
        boolean isAnyFragile = false;

        Map<UUID, Integer> cartProducts = shoppingCartDto.getProducts();
        Set<UUID> productIds = cartProducts.keySet();

        Map<UUID, WarehouseProduct> warehouseProductsMap = getWarehouseProductsMap(productIds);

        for (Map.Entry<UUID, Integer> entry : cartProducts.entrySet()) {
            UUID productId = entry.getKey();
            Integer requestedQuantity = entry.getValue();

            WarehouseProduct product = warehouseProductsMap.get(productId);
            if (product == null) {
                throw new NoSpecifiedProductInWarehouseException(
                        "Продукт с ID " + productId + " не существует"
                );
            }

            if (product.getQuantity() < requestedQuantity) {
                throw new ProductInShoppingCartLowQuantityInWarehouseException(
                        String.format("Не хватает товара %s. В корзине: %d, доступно: %d",
                                productId, requestedQuantity, product.getQuantity()));
            }
            BigDecimal qty = new BigDecimal(requestedQuantity);
            totalWeight = totalWeight.add(product.getWeight().multiply(qty));

            BigDecimal volume = product.getWidth()
                    .multiply(product.getHeight())
                    .multiply(product.getDepth())
                    .multiply(qty);
            totalVolume = totalVolume.add(volume);
            if (product.getFragile()) {
                isAnyFragile = true;
            }
        }

        return new BookedProductsDto(totalWeight.doubleValue(), totalVolume.doubleValue(), isAnyFragile);
    }

    @Override
    @Transactional
    public void addProductQuantity(AddProductToWarehouseRequest addProductDto) {
        UUID productId = addProductDto.getProductId();
        WarehouseProduct product = checkIdExistsOrThrow(productId);
        product.setQuantity(product.getQuantity() + addProductDto.getQuantity());
        warehouseRepository.save(product);
    }

    @Override
    public AddressDto getWarehouseAddress() {
        return new AddressDto(
                CURRENT_ADDRESS, // country
                CURRENT_ADDRESS, // city
                CURRENT_ADDRESS, // street
                CURRENT_ADDRESS, // house
                CURRENT_ADDRESS  // flat
        );
    }

    private WarehouseProduct checkIdExistsOrThrow(UUID productId) {
        log.info("Проверка существования productId={}", productId);
        return warehouseRepository.findById(productId).orElseThrow(() ->
                new NoSpecifiedProductInWarehouseException(
                        "Продукт с ID " + productId + " не существует"
                )
        );
    }

    private Map<UUID, WarehouseProduct> getWarehouseProductsMap(Set<UUID> productIds) {
        Map<UUID, WarehouseProduct> productsMap = new HashMap<>();
        for (WarehouseProduct product : warehouseRepository.findAllById(productIds)) {
            productsMap.put(product.getProductId(), product);
        }
        return productsMap;
    }
}
