package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.contract.OrderContract;
import ru.yandex.practicum.dto.CreateNewOrderRequest;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.ProductReturnRequest;
import ru.yandex.practicum.service.OrderService;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/order")
@RequiredArgsConstructor
@Slf4j
@Validated
public class OrderController implements OrderContract {
    private final OrderService orderService;

    @Override
    @PutMapping
    public OrderDto createOrder(@Valid @RequestBody CreateNewOrderRequest createNewOrderRequest) {
        log.info("Создать заказ {}", createNewOrderRequest);
        OrderDto orderDto = orderService.createOrder(createNewOrderRequest);
        log.info("Создан заказ {}", orderDto);
        return orderDto;
    }

    @Override
    @PostMapping("/return")
    public OrderDto returnOrder(@Valid @RequestBody ProductReturnRequest productReturnRequest) {
        log.info("Вернуть заказ {}", productReturnRequest);
        OrderDto orderDto = orderService.returnOrder(productReturnRequest);
        log.info("Вернули заказ {}", orderDto);
        return orderDto;
    }

    @Override
    @PostMapping("/payment")
    public OrderDto paymentComplete(@RequestBody UUID orderId) {
        log.info("Оплатить заказ {}", orderId);
        OrderDto orderDto = orderService.paymentComplete(orderId);
        log.info("Оплачен заказ {}", orderDto);
        return orderDto;
    }

    @Override
    @PostMapping("/payment/failed")
    public OrderDto paymentFailed(@RequestBody UUID orderId) {
        log.info("Оплата заказа {} произошла с ошибкой", orderId);
        OrderDto orderDto = orderService.paymentFailed(orderId);
        log.info("Оплата заказа {} с ошибкой оформлена", orderDto);
        return orderDto;
    }

    @Override
    @PostMapping("/delivery")
    public OrderDto orderDelivered(@RequestBody UUID orderId) {
        log.info("Доставка заказа {}", orderId);
        OrderDto orderDto = orderService.orderDelivered(orderId);
        log.info("Доставлен заказ {}", orderDto);
        return orderDto;
    }

    @Override
    @PostMapping("/delivery/failed")
    public OrderDto orderDeliveryFailed(@RequestBody UUID orderId) {
        log.info("Доставка заказа {} произошла с ошибкой", orderId);
        OrderDto orderDto = orderService.orderDeliveryFailed(orderId);
        log.info("Доставка заказа {} с ошибкой оформлена", orderDto);
        return orderDto;
    }

    @Override
    @PostMapping("/completed")
    public OrderDto orderCompleted(@RequestBody UUID orderId) {
        log.info("Завершение заказа {}", orderId);
        OrderDto orderDto = orderService.orderCompleted(orderId);
        log.info("Заказ {} завершен", orderDto);
        return orderDto;
    }

    @Override
    @PostMapping("/calculate/total")
    public OrderDto orderCalculateTotal(@RequestBody UUID orderId) {
        log.info("Расчёт стоимости заказа {}", orderId);
        OrderDto orderDto = orderService.orderCalculateTotal(orderId);
        log.info("Стоимость заказа {} рассчитана", orderDto);
        return orderDto;
    }

    @Override
    @PostMapping("/calculate/delivery")
    public OrderDto orderCalculateDelivery(@RequestBody UUID orderId) {
        log.info("Расчёт стоимости доставки заказа {}", orderId);
        OrderDto orderDto = orderService.orderCalculateDelivery(orderId);
        log.info("Стоимость доставки заказа {} рассчитана", orderDto);
        return orderDto;
    }

    @Override
    @PostMapping("/assembly")
    public OrderDto orderAssembled(@RequestBody UUID orderId) {
        log.info("Сборка заказа {}", orderId);
        OrderDto orderDto = orderService.orderAssembled(orderId);
        log.info("Сборка заказа {} завершена", orderDto);
        return orderDto;
    }

    @Override
    @PostMapping("/assembly/failed")
    public OrderDto orderAssembleFailed(@RequestBody UUID orderId) {
        log.info("Сборка заказа {} произошла с ошибкой", orderId);
        OrderDto orderDto = orderService.orderAssembleFailed(orderId);
        log.info("Сборка заказа с ошибкой {} оформлена", orderDto);
        return orderDto;
    }

    @Override
    @GetMapping
    public Page<OrderDto> getOrdersByUser(@RequestParam("username") @NotBlank String username,
                                          Pageable pageable) {
        log.info("Найти заказы пользователя {}", username);
        Page<OrderDto> orders = orderService.getOrdersByUser(username, pageable);
        log.info("Заказы пользователя {}: {}", username, orders);
        return orders;
    }

}
