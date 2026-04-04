package ru.yandex.practicum.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.dto.SetProductQuantityStateRequest;
import ru.yandex.practicum.enums.ProductCategory;

import java.util.UUID;

public interface ShoppingStoreService {
    ProductDto createProduct(ProductDto productDto);

    ProductDto updateProduct(ProductDto productDto);

    Boolean deleteProduct(UUID productId);

    Boolean setQuantityState(SetProductQuantityStateRequest request);

    ProductDto getProduct(UUID productId);

    Page<ProductDto> getProductsByCategory(ProductCategory productCategory, Pageable pageable);
}
