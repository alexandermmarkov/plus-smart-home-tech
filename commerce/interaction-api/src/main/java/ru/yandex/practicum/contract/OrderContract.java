package ru.yandex.practicum.contract;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.CreateNewOrderRequest;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.ProductReturnRequest;

import java.util.UUID;

public interface OrderContract {

    @PutMapping
    OrderDto createOrder(@Valid @RequestBody CreateNewOrderRequest createNewOrderRequest);
    @PostMapping("/return")
    OrderDto returnOrder(@Valid @RequestBody ProductReturnRequest productReturnRequest);
    @PostMapping("/payment")
    OrderDto paymentComplete(@RequestBody UUID orderId);
    @PostMapping("/payment/failed")
    OrderDto paymentFailed(@RequestBody UUID orderId);

    @PostMapping("/delivery")
    OrderDto orderDelivered(@RequestBody UUID orderId);
    @PostMapping("/delivery/failed")
    OrderDto orderDeliveryFailed(@RequestBody UUID orderId);
    @PostMapping("/completed")
    OrderDto orderCompleted(@RequestBody UUID orderId);
    @PostMapping("/calculate/total")
    OrderDto orderCalculateTotal(@RequestBody UUID orderId);
    @PostMapping("/calculate/delivery")
    OrderDto orderCalculateDelivery(@RequestBody UUID orderId);
    @PostMapping("/assembly")
    OrderDto orderAssembled(@RequestBody UUID orderId);
    @PostMapping("/assembly/failed")
    OrderDto orderAssembleFailed(@RequestBody UUID orderId);

    @GetMapping
    Page<OrderDto> getOrdersByUser(@RequestParam("username") String username,
                                   @SpringQueryMap Pageable pageable);
}
