package ru.yandex.practicum.contract;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.dto.*;

import java.util.Map;
import java.util.UUID;

public interface WarehouseContract {
    @PutMapping
    void addNewProductToWarehouse(@Valid @RequestBody NewProductInWarehouseRequest newProduct);

    @PostMapping("/check")
    BookedProductsDto checkAvailabilityForCart(@RequestBody ShoppingCartDto shoppingCartDto);

    @PostMapping("/add")
    void addProductQuantity(@Valid @RequestBody AddProductToWarehouseRequest addProductDto);

    @GetMapping("/address")
    AddressDto getWarehouseAddress();

    @PostMapping("/shipped")
    void ShippedToDelivery(@Valid @RequestBody ShippedToDeliveryRequest deliveryRequest);

    @PostMapping("/assembly")
    BookedProductsDto AssemblyToDelivery(@Valid @RequestBody AssemblyProductsForOrderRequest assemblyRequest);

    @PostMapping("/return")
    void returnProducts(@Valid @RequestBody Map<UUID, Integer> products);

}
