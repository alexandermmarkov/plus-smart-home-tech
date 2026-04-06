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
        UUID orderId = orderDto.getOrderId();

        log.info("Начало расчета стоимости доставки для заказа {}, weight={}, volume={}, fragile={}",
                orderId,
                orderDto.getDeliveryWeight(),
                orderDto.getDeliveryVolume(),
                orderDto.getFragile());

        Delivery delivery = getDeliveryByOrderIdOrThrow(orderId);
        AddressDto warehouseAddress = warehouseClient.getWarehouseAddress();

        log.info("Заказ {}. Адрес склада: country={}, city={}, street={}, house={}, flat={}",
                orderId,
                warehouseAddress.getCountry(),
                warehouseAddress.getCity(),
                warehouseAddress.getStreet(),
                warehouseAddress.getHouse(),
                warehouseAddress.getFlat());

        BigDecimal totalCost = coefficients.getBaseCost();
        log.info("Заказ {}. Базовая стоимость доставки: {}", orderId, totalCost);

        BigDecimal warehouseAddrCoef = BigDecimal.ZERO;
        if (isWarehouseAddressContains(warehouseAddress, "ADDRESS_1")) {
            warehouseAddrCoef = coefficients.getAddress1();
            log.info("Заказ {}. Применен коэффициент для ADDRESS_1: {}", orderId, warehouseAddrCoef);
        } else if (isWarehouseAddressContains(warehouseAddress, "ADDRESS_2")) {
            warehouseAddrCoef = coefficients.getAddress2();
            log.info("Заказ {}. Применен коэффициент для ADDRESS_2: {}", orderId, warehouseAddrCoef);
        } else {
            log.info("Заказ {}. Адресный коэффициент не применен", orderId);
        }

        totalCost = totalCost.add(totalCost.multiply(warehouseAddrCoef));
        log.info("Заказ {}. Стоимость после применения коэффициента адреса склада: {}", orderId, totalCost);

        if (Boolean.TRUE.equals(orderDto.getFragile())) {
            totalCost = totalCost.add(totalCost.multiply(coefficients.getFragile()));
            log.info("Заказ {}. Применена надбавка за хрупкость {}, стоимость: {}",
                    orderId, coefficients.getFragile(), totalCost);
        } else {
            log.info("Заказ {}. Надбавка за хрупкость не применена", orderId);
        }

        BigDecimal weightCost = coefficients.getWeight().multiply(BigDecimal.valueOf(orderDto.getDeliveryWeight()));
        totalCost = totalCost.add(weightCost);
        log.info("Заказ {}. Надбавка за вес: {}, стоимость после надбавки: {}",
                orderId, weightCost, totalCost);

        BigDecimal volumeCost = coefficients.getVolume().multiply(BigDecimal.valueOf(orderDto.getDeliveryVolume()));
        totalCost = totalCost.add(volumeCost);
        log.info("Заказ {}. Надбавка за объем: {}, стоимость после надбавки: {}",
                orderId, volumeCost, totalCost);

        if (!delivery.getFromAddress().getStreet().equals(warehouseAddress.getStreet())) {
            BigDecimal addressExtra = coefficients.getDeliveryAddress().multiply(totalCost);
            totalCost = totalCost.add(addressExtra);
            log.info("Заказ {}. Применена надбавка за различие адреса доставки и адреса склада: {}, стоимость: {}",
                    orderId, addressExtra, totalCost);
        } else {
            log.info("Заказ {}. Надбавка за различие адресов не применена", orderId);
        }

        log.info("Итоговая стоимость доставки для заказа {}: {}", orderId, totalCost);
        return totalCost;
    }

    private boolean isWarehouseAddressContains(AddressDto address, String str) {
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
