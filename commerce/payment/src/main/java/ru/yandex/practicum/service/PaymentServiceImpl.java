package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.client.DeliveryClient;
import ru.yandex.practicum.client.OrderClient;
import ru.yandex.practicum.client.ShoppingStoreClient;
import ru.yandex.practicum.config.PaymentCoefficientProperties;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.PaymentDto;
import ru.yandex.practicum.enums.PaymentState;
import ru.yandex.practicum.exceptions.ProductNotFoundException;
import ru.yandex.practicum.model.Payment;
import ru.yandex.practicum.model.PaymentMapper;
import ru.yandex.practicum.repository.PaymentRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    private final ShoppingStoreClient shoppingStoreClient;
    private final PaymentRepository paymentRepository;
    private final OrderClient orderClient;
    private final DeliveryClient deliveryClient;
    private final PaymentCoefficientProperties paymentCoefficientProperties;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional
    public PaymentDto makePayment(OrderDto orderDto) {
        BigDecimal productCost = getProductCost(orderDto);
        BigDecimal fee = calculateFee(productCost);
        BigDecimal deliveryCost = getDeliveryCost(orderDto);

        Payment payment = new Payment();
        payment.setOrderId(orderDto.getOrderId());
        payment.setTotalPayment(getTotalCost(orderDto));
        payment.setDeliveryTotal(deliveryCost);
        payment.setFeeTotal(fee);
        payment.setState(PaymentState.PENDING);

        Payment savedPayment = paymentRepository.save(payment);
        return paymentMapper.toDto(savedPayment);
    }

    @Override
    public BigDecimal getTotalCost(OrderDto orderDto) {
        BigDecimal productCost = getProductCost(orderDto);
        BigDecimal deliveryCost = getDeliveryCost(orderDto);
        BigDecimal nds = calculateFee(productCost);
        return productCost.add(deliveryCost).add(nds);
    }

    @Override
    @Transactional
    public void paymentSuccess(UUID paymentId) {
        Payment payment = checkIdExistsOrThrow(paymentId);
        payment.setState(PaymentState.SUCCESS);
        orderClient.paymentComplete(payment.getOrderId());
    }

    @Override
    public BigDecimal getProductCost(OrderDto orderDto) {
        BigDecimal productCost = BigDecimal.ZERO;
        for (Map.Entry<UUID, Integer> entry : orderDto.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            Integer quantity = entry.getValue();
            BigDecimal price = shoppingStoreClient.getProduct(productId).getPrice();
            productCost = productCost.add(price.multiply(BigDecimal.valueOf(quantity)));
        }
        return productCost;
    }

    @Override
    @Transactional
    public void paymentFailed(UUID paymentId) {
        // Идентификатор платежа
        Payment payment = checkIdExistsOrThrow(paymentId);
        payment.setState(PaymentState.FAILED);
        orderClient.paymentFailed(payment.getOrderId());
    }

    private BigDecimal calculateFee(BigDecimal productCost) {
        return productCost.multiply(paymentCoefficientProperties.getNds()
                .divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP)
        );
    }

    private BigDecimal getDeliveryCost(OrderDto orderDto) {
        BigDecimal deliveryCost = orderDto.getDeliveryPrice();
        if (deliveryCost == null || deliveryCost.equals(BigDecimal.ZERO)) {
            deliveryCost = deliveryClient.getDeliveryCost(orderDto);
        }
        return deliveryCost;
    }

    private Payment checkIdExistsOrThrow(UUID paymentId) {
        log.info("Проверка существования paymentId");
        return paymentRepository.findById(paymentId).orElseThrow(() ->
                new ProductNotFoundException("Платеж с ID " + paymentId + " не существует"));
    }
}
