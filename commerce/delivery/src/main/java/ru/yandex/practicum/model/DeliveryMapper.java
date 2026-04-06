package ru.yandex.practicum.model;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.DeliveryDto;

@Component
@RequiredArgsConstructor
public class DeliveryMapper {
    private final AddressMapper addressMapper;
    public Delivery toDelivery(DeliveryDto dto) {
        return Delivery.builder()
                .deliveryId(dto.getDeliveryId())
                .orderId(dto.getOrderId())
                .toAddress(addressMapper.toAddress(dto.getToAddress()))
                .fromAddress(addressMapper.toAddress(dto.getFromAddress()))
                .deliveryState(dto.getDeliveryState())
                .build();
    }

    public DeliveryDto toDto(Delivery delivery) {
        return DeliveryDto.builder()
                .deliveryId(delivery.getDeliveryId())
                .orderId(delivery.getOrderId())
                .toAddress(addressMapper.toDto(delivery.getToAddress()))
                .fromAddress(addressMapper.toDto(delivery.getFromAddress()))
                .deliveryState(delivery.getDeliveryState())
                .build();
    }
}
