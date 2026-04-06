package ru.yandex.practicum.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.yandex.practicum.enums.QuantityState;

import java.util.UUID;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SetProductQuantityStateRequest {
    @NotNull
    private UUID productId;
    @NotNull
    private QuantityState quantityState;
}
