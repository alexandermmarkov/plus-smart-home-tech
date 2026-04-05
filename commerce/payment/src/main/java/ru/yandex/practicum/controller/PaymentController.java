package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.contract.PaymentContract;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.PaymentDto;
import ru.yandex.practicum.service.PaymentService;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/payment")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PaymentController implements PaymentContract {
    private final PaymentService paymentService;

    @Override
    @PostMapping
    public PaymentDto makePayment(@Valid @RequestBody OrderDto orderDto) {
        log.info("Сформировать оплату для заказа {}", orderDto);
        PaymentDto paymentDto = paymentService.makePayment(orderDto);
        log.info("Сформирована оплата {}", paymentDto);
        return paymentDto;
    }

    @Override
    @PostMapping("/totalCost")
    public BigDecimal getTotalCost(@Valid @RequestBody OrderDto orderDto) {
        log.info("Рассчитать полную стоимость заказа {}", orderDto);
        BigDecimal totalCost = paymentService.getTotalCost(orderDto);
        log.info("Полная стоимость {}", totalCost);
        return totalCost;
    }

    @Override
    @PostMapping("/refund")
    public void paymentSuccess(@RequestBody UUID paymentId) {
        log.info("Эмуляция успешной оплаты платежа {}", paymentId);
        paymentService.paymentSuccess(paymentId);
        log.info("Успешная оплата платежа {} прошла", paymentId);
    }

    @Override
    @PostMapping("/productCost")
    public BigDecimal getProductCost(@RequestBody @Valid OrderDto orderDto) {
        log.info("Рассчитать стоимость товаров в заказе {}", orderDto);
        BigDecimal productCost = paymentService.getProductCost(orderDto);
        log.info("Стоимость товаров в заказе {}", productCost);
        return productCost;
    }

    @Override
    @PostMapping("/failed")
    public void paymentFailed(@RequestBody UUID paymentId) {
        log.info("Эмуляция отказа в оплате платежа {}", paymentId);
        paymentService.paymentFailed(paymentId);
        log.info("Эмуляция отказа в оплате платежа {} выполнена", paymentId);
    }
}
