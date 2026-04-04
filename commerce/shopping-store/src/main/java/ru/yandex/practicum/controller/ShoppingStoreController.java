package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.client.ShoppingStoreClient;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.dto.SetProductQuantityStateRequest;
import ru.yandex.practicum.enums.ProductCategory;
import ru.yandex.practicum.service.ShoppingStoreService;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/shopping-store")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ShoppingStoreController implements ShoppingStoreClient {

    private final ShoppingStoreService storeService;

    @Override
    @GetMapping
    public Page<ProductDto> getProductsByCategory(
            @RequestParam ProductCategory category, Pageable pageable) {
        log.info("Получить пагинированный список category {}, pageable {}", category, pageable);
        Page<ProductDto> products = storeService.getProductsByCategory(category, pageable);
        log.info("Возвращаем пагинированный список {}", products);
        return products;
    }

    @Override
    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public ProductDto createProduct(ProductDto productDto) {
        log.info("Создать продукт {}", productDto);
        ProductDto dto = storeService.createProduct(productDto);
        log.info("Возвращаем новый продукт {}", dto);
        return dto;
    }

    @Override
    @PostMapping
    public ProductDto updateProduct(ProductDto productDto) {
        log.info("Изменить продукт {}", productDto);
        ProductDto dto = storeService.updateProduct(productDto);
        log.info("Возвращаем измененный продукт {}", dto);
        return dto;
    }

    @Override
    @GetMapping("/{productId}")
    public ProductDto getProduct(@PathVariable UUID productId) {
        log.info("Найти продукт по ID = {}", productId);
        ProductDto dto = storeService.getProduct(productId);
        log.info("Возвращаем найденный продукт {}", dto);
        return dto;
    }

    @Override
    @PostMapping("/removeProductFromStore")
    public Boolean deleteProduct(@RequestBody UUID productId) {
        log.info("Удалить продукт по ID = {}", productId);
        Boolean result = storeService.deleteProduct(productId);
        log.info("Результат удаления продукта {} = {}", productId, result);
        return result;
    }

    @Override
    @PostMapping("/quantityState")
    public Boolean setQuantityState(SetProductQuantityStateRequest setProductQuantityStateRequest) {
        log.info("Изменить количество продукта {}", setProductQuantityStateRequest);
        Boolean result = storeService.setQuantityState(setProductQuantityStateRequest);
        log.info("Результат изменения количества продукта {}", result);
        return result;
    }
}
