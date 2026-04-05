package ru.yandex.practicum.model;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.OrderDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class OrderMapper {
    public OrderDto toDto(Order order) {
        return OrderDto.builder()
                .orderId(order.getOrderId())
                .shoppingCartId(order.getShoppingCartId())
                .products(productsToMap(order.getProducts()))
                .paymentId(order.getPaymentId())
                .orderState(order.getOrderState())
                .deliveryWeight(order.getDeliveryWeight())
                .deliveryVolume(order.getDeliveryVolume())
                .fragile(order.getFragile())
                .totalPrice(order.getTotalPrice())
                .deliveryPrice(order.getDeliveryPrice())
                .productPrice(order.getProductPrice())
                .build();
    }

    public List<OrderItem> productsToOrderItems(Map<UUID, Integer> products) {
        return products.entrySet().stream()
                .map(entry -> {
                    OrderItem item = new OrderItem();
                    item.setProductId(entry.getKey());
                    item.setQuantity(entry.getValue());
                    return item;
                })
                .toList();
    }
    public Map<UUID, Integer> productsToMap(List<OrderItem> products) {
        return products.stream()
                .collect(Collectors.toMap(
                        OrderItem::getProductId,
                        OrderItem::getQuantity
                ));
    }
}
