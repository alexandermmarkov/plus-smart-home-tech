package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.PaymentDto;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentService {
    PaymentDto makePayment(OrderDto orderDto);
    BigDecimal getTotalCost(OrderDto orderDto);
    void paymentSuccess(UUID paymentId);
    BigDecimal getProductCost(OrderDto order);
    void paymentFailed(UUID paymentId);
}
