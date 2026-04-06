package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.contract.WarehouseContract;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.service.WarehouseService;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/warehouse")
@RequiredArgsConstructor
@Slf4j
public class WarehouseController implements WarehouseContract {
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

    @Override
    @PostMapping("/shipped")
    public void ShippedToDelivery(@Valid @RequestBody ShippedToDeliveryRequest deliveryRequest) {
        log.info("Передать товары {} в доставку", deliveryRequest);
        warehouseService.shippedToDelivery(deliveryRequest);
        log.info("Товары {} переданы в доставку", deliveryRequest);
    }

    @Override
    @PostMapping("/assembly")
    public BookedProductsDto AssemblyToDelivery(@Valid @RequestBody AssemblyProductsForOrderRequest assemblyRequest) {
        log.info("Собрать товары {} к заказу {} для подготовки к отправке",
                assemblyRequest.getProducts(), assemblyRequest.getOrderId());
        BookedProductsDto bookedProductsDto = warehouseService.assemblyToDelivery(assemblyRequest);
        log.info("Товары {} к заказу {} для подготовки к отправке собраны: {}",
                assemblyRequest.getProducts(), assemblyRequest.getOrderId(), bookedProductsDto);
        return null;
    }

    @Override
    @PostMapping("/return")
    public void returnProducts(@Valid @RequestBody Map<UUID, Integer> products) {
        log.info("Вернуть товары на склад {}", products);


    }

}
