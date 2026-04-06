package ru.yandex.practicum.model;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.PaymentDto;

@Component
public class PaymentMapper {

    public PaymentDto toDto(Payment payment) {
        return PaymentDto.builder()
                .paymentId(payment.getPaymentId())
                .totalPayment(payment.getTotalPayment())
                .deliveryTotal(payment.getDeliveryTotal())
                .feeTotal(payment.getFeeTotal())
                .build();
    }
}
