package ru.yandex.practicum.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDto {
    private UUID paymentId;
    @Positive
    private BigDecimal totalPayment;
    @PositiveOrZero
    private BigDecimal deliveryTotal;
    @PositiveOrZero
    private BigDecimal feeTotal;
}
