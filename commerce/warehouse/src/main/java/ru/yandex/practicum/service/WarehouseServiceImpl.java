package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.exceptions.*;
import ru.yandex.practicum.model.OrderBooking;
import ru.yandex.practicum.model.WarehouseProduct;
import ru.yandex.practicum.model.WarehouseProductMapper;
import ru.yandex.practicum.repository.OrderBookingRepository;
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
    private final OrderBookingRepository orderBookingRepository;

    private static final String[] ADDRESSES = new String[]{"ADDRESS_1", "ADDRESS_2"};
    private static final String CURRENT_ADDRESS =
            ADDRESSES[Random.from(new SecureRandom()).nextInt(0, ADDRESSES.length)];

    @Override
    @Transactional
    public void addNewProductToWarehouse(NewProductInWarehouseRequest newProduct) {
        if (warehouseRepository.existsById(newProduct.getProductId())) {
            throw new SpecifiedProductAlreadyInWarehouseException(
                    "Товар с таким описанием уже зарегистрирован на складе"
            );
        }
        WarehouseProduct product = warehouseMapper.toModel(newProduct);
        warehouseRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public BookedProductsDto checkAvailabilityForCart(ShoppingCartDto shoppingCartDto) {
        log.info("Проверка достаточного количества товаров для корзины {}", shoppingCartDto.getShoppingCartId());
        return checkAvailabilityForProductsMap(shoppingCartDto.getProducts());
    }

    @Override
    @Transactional
    public void addProductQuantity(AddProductToWarehouseRequest addProductDto) {
        UUID productId = addProductDto.getProductId();
        WarehouseProduct product = checkIdExistsOrThrow(productId);
        product.setQuantity(product.getQuantity() + addProductDto.getQuantity());
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

    @Override
    @Transactional
    public void shippedToDelivery(ShippedToDeliveryRequest deliveryRequest) {
        UUID orderId = deliveryRequest.getOrderId();
        OrderBooking orderBooking = orderBookingRepository.findByOrderId(orderId)
                .orElseThrow(() ->
                        new NoOrderFoundException("Бронирование для заказа " + orderId + " не найдено"));

        orderBooking.setDeliveryId(deliveryRequest.getDeliveryId());
        orderBookingRepository.save(orderBooking);
    }

    @Override
    @Transactional
    public void returnProducts(Map<UUID, Integer> products) {
        Map<UUID, WarehouseProduct> warehouseProductsMap =
                getWarehouseProductsMapWithLock(products.keySet());

        for (Map.Entry<UUID, Integer> entry : products.entrySet()) {
            UUID productId = entry.getKey();
            Integer returnedQuantity = entry.getValue();

            WarehouseProduct product = warehouseProductsMap.get(productId);
            if (product == null) {
                throw new ProductNotFoundException("Продукт с ID " + productId + " не существует");
            }

            product.setQuantity(product.getQuantity() + returnedQuantity);
        }
    }

    @Override
    @Transactional
    public BookedProductsDto assemblyToDelivery(AssemblyProductsForOrderRequest assemblyRequest) {
        Map<UUID, Integer> requestedProducts = assemblyRequest.getProducts();

        Map<UUID, WarehouseProduct> warehouseProductsMap =
                getWarehouseProductsMapWithLock(requestedProducts.keySet());

        BookedProductsDto bookedProductsDto =
                calculateAndValidateBookedProducts(requestedProducts, warehouseProductsMap);

        UUID orderId = assemblyRequest.getOrderId();

        OrderBooking orderBooking = orderBookingRepository.findByOrderId(orderId)
                .orElseGet(() -> OrderBooking.builder().orderId(orderId).build());

        orderBooking.setDeliveryWeight(BigDecimal.valueOf(bookedProductsDto.getDeliveryWeight()));
        orderBooking.setDeliveryVolume(BigDecimal.valueOf(bookedProductsDto.getDeliveryVolume()));
        orderBooking.setFragile(bookedProductsDto.getFragile());
        orderBooking.getBookingProducts().clear();

        for (Map.Entry<UUID, Integer> entry : requestedProducts.entrySet()) {
            UUID productId = entry.getKey();
            Integer requestedQuantity = entry.getValue();

            WarehouseProduct product = warehouseProductsMap.get(productId);
            if (product == null) {
                throw new ProductNotFoundException("Продукт с ID " + productId + " не существует");
            }

            product.setQuantity(product.getQuantity() - requestedQuantity);
            orderBooking.addBookingProduct(product, requestedQuantity);
        }

        orderBookingRepository.save(orderBooking);
        return bookedProductsDto;
    }

    private BookedProductsDto checkAvailabilityForProductsMap(Map<UUID, Integer> products) {
        Map<UUID, WarehouseProduct> warehouseProductsMap =
                getWarehouseProductsMap(products.keySet());

        return calculateAndValidateBookedProducts(products, warehouseProductsMap);
    }

    private BookedProductsDto calculateAndValidateBookedProducts(
            Map<UUID, Integer> requestedProducts,
            Map<UUID, WarehouseProduct> warehouseProductsMap
    ) {
        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal totalVolume = BigDecimal.ZERO;
        boolean isAnyFragile = false;

        for (Map.Entry<UUID, Integer> entry : requestedProducts.entrySet()) {
            UUID productId = entry.getKey();
            Integer requestedQuantity = entry.getValue();

            WarehouseProduct product = warehouseProductsMap.get(productId);
            if (product == null) {
                throw new NoSpecifiedProductInWarehouseException(
                        "Продукт с ID " + productId + " не существует"
                );
            }

            checkEnoughQuantityOrThrow(product, requestedQuantity);

            BigDecimal qty = BigDecimal.valueOf(requestedQuantity);
            totalWeight = totalWeight.add(product.getWeight().multiply(qty));

            BigDecimal volume = product.getWidth()
                    .multiply(product.getHeight())
                    .multiply(product.getDepth())
                    .multiply(qty);
            totalVolume = totalVolume.add(volume);

            if (Boolean.TRUE.equals(product.getFragile())) {
                isAnyFragile = true;
            }
        }

        return new BookedProductsDto(
                totalWeight.doubleValue(),
                totalVolume.doubleValue(),
                isAnyFragile
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

    private void checkEnoughQuantityOrThrow(WarehouseProduct product, Integer requestedQuantity) {
        log.info("Проверка достаточного количества товара productId={}", product.getProductId());
        if (product.getQuantity() < requestedQuantity) {
            throw new ProductInShoppingCartLowQuantityInWarehouseException(
                    String.format(
                            "Не хватает товара %s. Требуется: %d, доступно: %d",
                            product.getProductId(),
                            requestedQuantity,
                            product.getQuantity()
                    )
            );
        }
    }

    private Map<UUID, WarehouseProduct> getWarehouseProductsMap(Set<UUID> productIds) {
        return toProductMap(warehouseRepository.findAllById(productIds));
    }

    private Map<UUID, WarehouseProduct> getWarehouseProductsMapWithLock(Set<UUID> productIds) {
        return toProductMap(warehouseRepository.findAllWithLockByProductIdIn(productIds));
    }

    private Map<UUID, WarehouseProduct> toProductMap(Collection<WarehouseProduct> products) {
        Map<UUID, WarehouseProduct> result = new HashMap<>();
        for (WarehouseProduct product : products) {
            result.put(product.getProductId(), product);
        }
        return result;
    }
}
