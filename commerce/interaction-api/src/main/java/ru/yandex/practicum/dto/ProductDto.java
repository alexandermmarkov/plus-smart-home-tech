package ru.yandex.practicum.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.enums.ProductCategory;
import ru.yandex.practicum.enums.ProductState;
import ru.yandex.practicum.enums.QuantityState;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto {
    private UUID productId;
    @NotBlank(message = "Не заполнено Наименование товара")
    private String productName;
    @NotBlank(message = "Не заполнено Описание товара")
    private String description;
    private String imageSrc;
    @NotNull(message = "Количество остатка не может быть null")
    private QuantityState quantityState;
    @NotNull(message = "Статус товара не может быть null")
    private ProductState productState;
    @NotNull(message = "Категория товара не может быть null")
    private ProductCategory productCategory;
    @NotNull(message = "Не заполнена цена")
    @DecimalMin(value = "1.0", message = "Цена должна быть >= 1")
    private BigDecimal price;

}
