package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.dto.SetProductQuantityStateRequest;
import ru.yandex.practicum.enums.ProductCategory;
import ru.yandex.practicum.enums.ProductState;
import ru.yandex.practicum.exceptions.ProductNotFoundException;
import ru.yandex.practicum.exceptions.ValidateException;
import ru.yandex.practicum.model.Product;
import ru.yandex.practicum.model.ProductMapper;
import ru.yandex.practicum.repository.ProductRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShoppingStoreServiceImpl implements ShoppingStoreService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductDto createProduct(ProductDto productDto) {
        UUID productId = productDto.getProductId();
        if (productId != null) {
            checkIdIsNewOrThrow(productId);
        }
        Product product = productMapper.toModel(productDto);
        Product newProduct = productRepository.save(product);
        return productMapper.toDto(newProduct);
    }

    @Override
    @Transactional
    public ProductDto updateProduct(ProductDto productDto) {
        UUID productId = productDto.getProductId();
        checkIdExistsOrThrow(productId);
        Product product = productMapper.toModel(productDto);
        Product newProduct = productRepository.save(product);
        return productMapper.toDto(newProduct);
    }

    @Override
    @Transactional
    public Boolean deleteProduct(UUID productId) {
        checkIdExistsOrThrow(productId);
        Product product = productRepository.getReferenceById(productId);
        product.setProductState(ProductState.DEACTIVATE);
        Product newProduct = productRepository.save(product);
        return newProduct.getProductState().equals(ProductState.DEACTIVATE);
    }

    @Override
    @Transactional
    public Boolean setQuantityState(SetProductQuantityStateRequest request) {
        UUID productId = request.getProductId();
        checkIdExistsOrThrow(productId);
        Product product = productRepository.getReferenceById(productId);
        product.setQuantityState(request.getQuantityState());
        Product newProduct = productRepository.save(product);
        return newProduct.getQuantityState().equals(request.getQuantityState());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto getProduct(UUID productId) {
        Product product = checkIdExistsOrThrow(productId);
        return productMapper.toDto(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> getProductsByCategory(ProductCategory productCategory, Pageable pageable) {
        return productRepository.findAllByProductCategory(productCategory, pageable)
                .map(productMapper::toDto);
    }

    private void checkIdIsNewOrThrow(UUID productId) {
        log.info("Проверка неповторяемости productId");
        if (productRepository.existsById(productId)) {
            throw new ValidateException(
                    String.format("Продукт с ID %s уже существует", productId)
            );
        }
    }

    private Product checkIdExistsOrThrow(UUID productId) {
        log.info("Проверка существования productId");
        Product product = productRepository.findById(productId).orElseThrow(() ->
                new ProductNotFoundException("Продукт с ID " + productId + " не существует"));
        return product;
    }

}
