package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.client.WarehouseClient;
import ru.yandex.practicum.dto.AddProductToWarehouseRequest;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.BookedProductsDto;
import ru.yandex.practicum.dto.NewProductInWarehouseRequest;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.service.WarehouseService;

@RestController
@RequestMapping(path = "/api/v1/warehouse")
@RequiredArgsConstructor
@Slf4j
public class WarehouseController implements WarehouseClient {
    private final WarehouseService warehouseService;

    @Override
    @PutMapping
    public void addNewProductToWarehouse(@Valid @RequestBody NewProductInWarehouseRequest newProduct) {
        log.info("Создать новый продукт {}", newProduct);
        warehouseService.addNewProductToWarehouse(newProduct);
        log.info("Новый продукт {} создан", newProduct);
    }

    @Override
    @PostMapping("/check")
    public BookedProductsDto checkAvailabilityForCart(@RequestBody ShoppingCartDto shoppingCartDto) {
        log.info("Проверить наличие товаров на складе для корзины {}", shoppingCartDto);
        BookedProductsDto bookedProductsDto = warehouseService.checkAvailabilityForCart(shoppingCartDto);
        log.info("Количество товаров на складе {}", bookedProductsDto);
        return bookedProductsDto;
    }

    @Override
    @PostMapping("/add")
    public void addProductQuantity(@Valid @RequestBody AddProductToWarehouseRequest addProductDto) {
        log.info("Принять товар на склад: {}", addProductDto);
        warehouseService.addProductQuantity(addProductDto);
        log.info("Товар {} принят на склад", addProductDto);
    }

    @Override
    @GetMapping("/address")
    public AddressDto getWarehouseAddress() {
        log.info("Узнать адрес склада");
        AddressDto addressDto = warehouseService.getWarehouseAddress();
        log.info("Адрес склада {}", addressDto);
        return addressDto;
    }

}
