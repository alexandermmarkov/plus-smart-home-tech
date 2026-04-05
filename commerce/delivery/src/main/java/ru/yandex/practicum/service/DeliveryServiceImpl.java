package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.client.OrderClient;
import ru.yandex.practicum.client.WarehouseClient;
import ru.yandex.practicum.config.DeliveryCoefficientProperties;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.DeliveryDto;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.ShippedToDeliveryRequest;
import ru.yandex.practicum.enums.DeliveryState;
import ru.yandex.practicum.exceptions.NoDeliveryFoundException;
import ru.yandex.practicum.model.Delivery;
import ru.yandex.practicum.model.DeliveryMapper;
import ru.yandex.practicum.repository.DeliveryRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryServiceImpl implements DeliveryService {
    private final DeliveryMapper deliveryMapper;
    private final DeliveryRepository deliveryRepository;
    private final OrderClient orderClient;
    private final WarehouseClient warehouseClient;
    private final DeliveryCoefficientProperties coefficients;

    @Override
    @Transactional
    public DeliveryDto createDelivery(DeliveryDto deliveryDto) {
        Delivery delivery = deliveryMapper.toDelivery(deliveryDto);
        delivery.setDeliveryState(DeliveryState.CREATED);
        Delivery newDelivery = deliveryRepository.save(delivery);
        return deliveryMapper.toDto(newDelivery);
    }

    @Override
    @Transactional
    public void deliverySuccessful(UUID orderId) {
        // Проставить признак успешной доставки
        // Идентификатор заказа
        Delivery delivery = getDeliveryByOrderIdOrThrow(orderId);
        delivery.setDeliveryState(DeliveryState.DELIVERED);
    }

    @Override
    @Transactional
    public void deliveryPicked(UUID orderId) {
        // Принять товары в доставку
        Delivery delivery = getDeliveryByOrderIdOrThrow(orderId);
        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);
        // изменить статус заказа на ASSEMBLED в сервисе заказов orderAssembled
        orderClient.orderAssembled(orderId);
        // связать идентификатор доставки с внутренней учётной системой через вызов соответствующего метода склада
        ShippedToDeliveryRequest deliveryRequest = new ShippedToDeliveryRequest(orderId, delivery.getDeliveryId());
        warehouseClient.ShippedToDelivery(deliveryRequest);
    }

    @Override
    @Transactional
    public void deliveryFailed(UUID orderId) {
        // Установить признак ошибки в доставк
        Delivery delivery = getDeliveryByOrderIdOrThrow(orderId);
        delivery.setDeliveryState(DeliveryState.FAILED);
    }

    @Override
    public BigDecimal getDeliveryCost(OrderDto orderDto) {
        // Рассчитать стоимость доставки заказа
        Delivery delivery = getDeliveryByOrderIdOrThrow(orderDto.getOrderId());
        BigDecimal totalCost = coefficients.getBaseCost();
        BigDecimal warehouseAddrCoef = BigDecimal.ZERO;
        if (isWarehouseAddressContains("ADDRESS_1")) {
            warehouseAddrCoef = coefficients.getAddress1();
        } else if (isWarehouseAddressContains("ADDRESS_2")) {
            warehouseAddrCoef = coefficients.getAddress2();
        }
        totalCost = totalCost.add(totalCost.multiply(warehouseAddrCoef));
        if (orderDto.getFragile()) {
            totalCost = totalCost.add(totalCost.multiply(coefficients.getFragile()));
        }
        totalCost = totalCost.add(coefficients.getWeight().multiply(BigDecimal.valueOf(orderDto.getDeliveryWeight())));
        totalCost = totalCost.add(coefficients.getVolume().multiply(BigDecimal.valueOf(orderDto.getDeliveryVolume())));
        if (!delivery.getFromAddress().getStreet().equals(warehouseClient.getWarehouseAddress().getStreet())) {
            totalCost = totalCost.add(coefficients.getDeliveryAddress().multiply(totalCost));
        }
        return totalCost;
    }

    private boolean isWarehouseAddressContains(String str) {
        AddressDto address = warehouseClient.getWarehouseAddress();
        return (address.getStreet().contains(str)
                || address.getCountry().contains(str)
                || address.getCity().contains(str)
                || address.getHouse().contains(str)
                || address.getFlat().contains(str)
        );
    }

    private Delivery getDeliveryByOrderIdOrThrow(UUID orderId) {
        return deliveryRepository.findByOrderId(orderId).orElseThrow(() ->
                new NoDeliveryFoundException("Доставка по заказу с ID " + orderId + " не существует"));
    }
}
