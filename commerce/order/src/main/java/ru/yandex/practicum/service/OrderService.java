package ru.yandex.practicum.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.dto.CreateNewOrderRequest;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.ProductReturnRequest;

import java.util.UUID;

public interface OrderService {
    OrderDto createOrder(CreateNewOrderRequest createNewOrderRequest);

    OrderDto returnOrder(ProductReturnRequest productReturnRequest);

    OrderDto paymentComplete(UUID orderId);

    OrderDto paymentFailed(UUID orderId);

    OrderDto orderDelivered(UUID orderId);

    OrderDto orderDeliveryFailed(UUID orderId);

    OrderDto orderCompleted(UUID orderId);

    OrderDto orderCalculateTotal(UUID orderId);

    OrderDto orderCalculateDelivery(UUID orderId);

    OrderDto orderAssembled(UUID orderId);

    OrderDto orderAssembleFailed(UUID orderId);

    Page<OrderDto> getOrdersByUser(String username, Pageable pageable);

}
