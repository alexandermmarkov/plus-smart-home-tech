package ru.yandex.practicum.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class CreateNewOrderRequest {
    @NotNull
    private ShoppingCartDto shoppingCart;
    @NotNull
    private AddressDto deliveryAddress;
}
