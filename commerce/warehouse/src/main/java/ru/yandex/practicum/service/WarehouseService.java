package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.*;

import java.util.Map;
import java.util.UUID;

public interface WarehouseService {
    void addNewProductToWarehouse(NewProductInWarehouseRequest newProduct);

    BookedProductsDto checkAvailabilityForCart(ShoppingCartDto shoppingCartDto);

    void addProductQuantity(AddProductToWarehouseRequest addProductDto);

    AddressDto getWarehouseAddress();

    void shippedToDelivery(ShippedToDeliveryRequest deliveryRequest);

    BookedProductsDto assemblyToDelivery(AssemblyProductsForOrderRequest assemblyRequest);

    void returnProducts(Map<UUID, Integer> products);
}
