package ru.yandex.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.contract.DeliveryContract;

@FeignClient(name = "delivery", path = "/api/v1/delivery")
public interface DeliveryClient extends DeliveryContract {
}
