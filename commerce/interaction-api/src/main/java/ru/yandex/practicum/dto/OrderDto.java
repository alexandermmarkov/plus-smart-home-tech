package ru.yandex.practicum.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.enums.OrderState;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class OrderDto {
    private UUID orderId;
    private UUID shoppingCartId;
    @NotNull
    private Map<UUID, Integer> products;
    private UUID paymentId;
    private UUID deliveryId;
    private OrderState orderState;
    private Double deliveryWeight;
    private Double deliveryVolume;
    private Boolean fragile;
    @Positive
    private BigDecimal totalPrice;
    @PositiveOrZero
    private BigDecimal deliveryPrice;
    @Positive
    private BigDecimal productPrice;
}
