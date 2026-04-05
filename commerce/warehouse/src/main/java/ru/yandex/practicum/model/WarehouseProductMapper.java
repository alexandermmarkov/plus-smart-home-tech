package ru.yandex.practicum.model;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.NewProductInWarehouseRequest;

import java.math.BigDecimal;

@Component
public class WarehouseProductMapper {
    public WarehouseProduct toModel(NewProductInWarehouseRequest dto) {
        return WarehouseProduct.builder()
                .productId(dto.getProductId())
                .quantity(0)
                .weight(toBigDecimal(dto.getWeight()))
                .width(toBigDecimal(dto.getDimension().getWidth()))
                .height(toBigDecimal(dto.getDimension().getHeight()))
                .depth(toBigDecimal(dto.getDimension().getDepth()))
                .fragile(dto.getFragile())
                .build();
    }

    private BigDecimal toBigDecimal(Double value) {
        if (value == null) {
            return null;
        }
        return BigDecimal.valueOf(value);
    }
}
