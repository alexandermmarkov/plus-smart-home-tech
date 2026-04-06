package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.client.ShoppingCartClient;
import ru.yandex.practicum.client.WarehouseClient;
import ru.yandex.practicum.dto.CreateNewOrderRequest;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.ProductReturnRequest;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.enums.OrderState;
import ru.yandex.practicum.exceptions.NoOrderFoundException;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.model.OrderItem;
import ru.yandex.practicum.model.OrderMapper;
import ru.yandex.practicum.repository.OrderRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final WarehouseClient warehouseClient;
    private final OrderMapper orderMapper;
    private final ShoppingCartClient shoppingCartClient;

    @Override
    @Transactional
    public OrderDto createOrder(CreateNewOrderRequest createNewOrderRequest) {
        ShoppingCartDto shoppingCartDto = createNewOrderRequest.getShoppingCart();

        warehouseClient.checkAvailabilityForCart(shoppingCartDto);

        List<OrderItem> orderItems = orderMapper.productsToOrderItems(shoppingCartDto.getProducts());

        Order order = Order.builder()
                .shoppingCartId(shoppingCartDto.getShoppingCartId())
                .products(orderItems)
                .orderState(OrderState.NEW)
                .build();

        Order newOrder = orderRepository.save(order);
        return orderMapper.toDto(newOrder);
    }

    @Override
    @Transactional
    public OrderDto returnOrder(ProductReturnRequest productReturnRequest) {
        UUID orderId = productReturnRequest.getOrderId();
        Order order = getOrderOrThrow(orderId);
        order.setOrderState(OrderState.PRODUCT_RETURNED);
        Map<UUID, Integer> products = productReturnRequest.getProducts();
        warehouseClient.returnProducts(products);
        return orderMapper.toDto(order);
    }

    @Override
    @Transactional
    public OrderDto paymentComplete(UUID orderId) {
        Order order = getOrderOrThrow(orderId);
        order.setOrderState(OrderState.PAID);
        return orderMapper.toDto(order);
    }

    @Override
    @Transactional
    public OrderDto paymentFailed(UUID orderId) {
        Order order = getOrderOrThrow(orderId);
        order.setOrderState(OrderState.PAYMENT_FAILED);
        return orderMapper.toDto(order);
    }

    @Override
    @Transactional
    public OrderDto orderDelivered(UUID orderId) {
        Order order = getOrderOrThrow(orderId);
        order.setOrderState(OrderState.DELIVERED);
        return orderMapper.toDto(order);
    }

    @Override
    @Transactional
    public OrderDto orderDeliveryFailed(UUID orderId) {
        Order order = getOrderOrThrow(orderId);
        order.setOrderState(OrderState.DELIVERY_FAILED);
        return orderMapper.toDto(order);
    }

    @Override
    @Transactional
    public OrderDto orderCompleted(UUID orderId) {
        Order order = getOrderOrThrow(orderId);
        order.setOrderState(OrderState.COMPLETED);
        return orderMapper.toDto(order);
    }

    @Override
    @Transactional
    public OrderDto orderCalculateTotal(UUID orderId) {
        Order order = getOrderOrThrow(orderId);
        order.setOrderState(OrderState.ON_PAYMENT);
        return orderMapper.toDto(order);
    }

    @Override
    @Transactional
    public OrderDto orderCalculateDelivery(UUID orderId) {
        Order order = getOrderOrThrow(orderId);
        order.setOrderState(OrderState.ON_DELIVERY);
        return orderMapper.toDto(order);
    }

    @Override
    @Transactional
    public OrderDto orderAssembled(UUID orderId) {
        Order order = getOrderOrThrow(orderId);
        order.setOrderState(OrderState.ASSEMBLED);
        return orderMapper.toDto(order);
    }

    @Override
    @Transactional
    public OrderDto orderAssembleFailed(UUID orderId) {
        Order order = getOrderOrThrow(orderId);
        order.setOrderState(OrderState.ASSEMBLY_FAILED);
        return orderMapper.toDto(order);
    }

    @Override
    public Page<OrderDto> getOrdersByUser(String username, Pageable pageable) {
        ShoppingCartDto shoppingCartDto = shoppingCartClient.getActiveCart(username);
        UUID shoppingCartId = shoppingCartDto.getShoppingCartId();
        Page<Order> orders = orderRepository.getAllByShoppingCartId(shoppingCartId, pageable);
        return orders.map(orderMapper::toDto);
    }

    private Order getOrderOrThrow(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException("Заказ " + orderId + " не найден"));
    }

}
