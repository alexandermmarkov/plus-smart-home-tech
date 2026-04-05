package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.contract.DeliveryContract;
import ru.yandex.practicum.dto.DeliveryDto;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.service.DeliveryService;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/delivery")
@RequiredArgsConstructor
@Slf4j
@Validated
public class DeliveryController implements DeliveryContract {
    private final DeliveryService deliveryService;

    @Override
    @PutMapping
    public DeliveryDto createDelivery(@Valid @RequestBody DeliveryDto deliveryDto) {
        log.info("Создать новую доставку {}", deliveryDto);
        DeliveryDto newDeliveryDto = deliveryService.createDelivery(deliveryDto);
        log.info("Создана новая доставка {}", newDeliveryDto);
        return newDeliveryDto;
    }

    @Override
    @PostMapping("/successful")
    public void deliverySuccessful(@RequestBody UUID orderId) {
        log.info("Эмуляция успешной доставки заказа {}", orderId);
        deliveryService.deliverySuccessful(orderId);
        log.info("Выполнена эмуляция успешной доставки заказа {}", orderId);
    }

    @Override
    @PostMapping("/picked")
    public void deliveryPicked(@RequestBody UUID orderId) {
        log.info("Эмуляция получения товара в доставку заказа {}", orderId);
        deliveryService.deliveryPicked(orderId);
        log.info("Выполнена эмуляция получения товара в доставку заказа {}", orderId);
    }

    @Override
    @PostMapping("/failed")
    public void deliveryFailed(@RequestBody UUID orderId) {
        log.info("Эмуляция неудачного вручения заказа {}", orderId);
        deliveryService.deliveryFailed(orderId);
        log.info("Выполнена эмуляция неудачного вручения заказа {}", orderId);
    }

    @Override
    @PostMapping("/cost")
    public BigDecimal getDeliveryCost(@RequestBody @Valid OrderDto orderDto) {
        log.info("Рассчитать полную стоимость доставки заказа {}", orderDto);
        BigDecimal deliveryCost = deliveryService.getDeliveryCost(orderDto);
        log.info("Стоимость доставки заказа {}", deliveryCost);
        return deliveryCost;
    }
}
