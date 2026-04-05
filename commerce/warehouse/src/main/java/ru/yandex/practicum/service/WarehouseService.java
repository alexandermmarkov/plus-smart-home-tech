package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.AddProductToWarehouseRequest;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.BookedProductsDto;
import ru.yandex.practicum.dto.NewProductInWarehouseRequest;
import ru.yandex.practicum.dto.ShoppingCartDto;

public interface WarehouseService {
    void addNewProductToWarehouse(NewProductInWarehouseRequest newProduct);

    BookedProductsDto checkAvailabilityForCart(ShoppingCartDto shoppingCartDto);

    void addProductQuantity(AddProductToWarehouseRequest addProductDto);

    AddressDto getWarehouseAddress();
}
